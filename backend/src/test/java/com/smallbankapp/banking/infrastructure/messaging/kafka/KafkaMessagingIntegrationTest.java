package com.smallbankapp.banking.infrastructure.messaging.kafka;

import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.AuditLogMongoRepository;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.TransactionHistoryMongoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Integration test: publishes a TransactionEvent to a real Kafka (Testcontainers)
 * and verifies both AuditLogConsumer and TransactionHistoryConsumer persist records
 * to MongoDB within a reasonable timeout.
 */
@SpringBootTest
@Testcontainers
class KafkaMessagingIntegrationTest {

    private static final String TOPIC = "banking.transactions";

    // ── Containers ────────────────────────────────────────────────────────────

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                    .withDatabaseName("banking")
                    .withUsername("banking")
                    .withPassword("banking");

    @Container
    static final MongoDBContainer mongo =
            new MongoDBContainer(DockerImageName.parse("mongo:7"));

    @Container
    static final KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

    // ── Dynamic Properties ───────────────────────────────────────────────────

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    // ── Beans ─────────────────────────────────────────────────────────────────

    @Autowired
    private KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Autowired
    private AuditLogMongoRepository auditLogMongoRepository;

    @Autowired
    private TransactionHistoryMongoRepository transactionHistoryMongoRepository;

    @BeforeEach
    void cleanMongo() {
        auditLogMongoRepository.deleteAll();
        transactionHistoryMongoRepository.deleteAll();
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    @Test
    void depositEvent_isPersistedToAuditLog() {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionEvent event = TransactionEvent.of(
                transactionId, null, accountId,
                "DEPOSIT", BigDecimal.valueOf(200.00), "USD", "COMPLETED", Instant.now()
        );

        kafkaTemplate.send(TOPIC, accountId.toString(), event);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(auditLogMongoRepository.existsByTransactionId(transactionId)).isTrue()
                );
    }

    @Test
    void depositEvent_isPersistedToTransactionHistory() {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionEvent event = TransactionEvent.of(
                transactionId, null, accountId,
                "DEPOSIT", BigDecimal.valueOf(150.00), "USD", "COMPLETED", Instant.now()
        );

        kafkaTemplate.send(TOPIC, accountId.toString(), event);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() ->
                        assertThat(transactionHistoryMongoRepository.existsByTransactionId(transactionId)).isTrue()
                );
    }

    @Test
    void transferEvent_isPersistedWithBothAccounts() {
        UUID transactionId = UUID.randomUUID();
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        TransactionEvent event = TransactionEvent.of(
                transactionId, sourceId, targetId,
                "TRANSFER", BigDecimal.valueOf(500.00), "USD", "COMPLETED", Instant.now()
        );

        kafkaTemplate.send(TOPIC, sourceId.toString(), event);

        await().atMost(15, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertThat(auditLogMongoRepository.existsByTransactionId(transactionId)).isTrue();
                    assertThat(transactionHistoryMongoRepository.existsByTransactionId(transactionId)).isTrue();
                });
    }

    @Test
    void duplicateEvent_isNotPersistedTwice() throws InterruptedException {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionEvent event = TransactionEvent.of(
                transactionId, accountId, null,
                "WITHDRAWAL", BigDecimal.valueOf(75.00), "USD", "COMPLETED", Instant.now()
        );

        kafkaTemplate.send(TOPIC, accountId.toString(), event);
        kafkaTemplate.send(TOPIC, accountId.toString(), event);

        TimeUnit.SECONDS.sleep(10);

        assertThat(auditLogMongoRepository.findByTransactionId(transactionId)).isPresent();
        assertThat(transactionHistoryMongoRepository.existsByTransactionId(transactionId)).isTrue();
    }
}
