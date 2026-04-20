package com.smallbankapp.banking.infrastructure.messaging.kafka.consumer;

import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import com.smallbankapp.banking.infrastructure.persistence.mongo.document.AuditLogDocument;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.AuditLogMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Kafka consumer for audit logs.
 * Active on profiles: dev, docker, test.
 * On aws/localstack profiles, SqsAuditLogConsumer takes over.
 */
@Slf4j
@Component
@Profile({"dev", "docker", "test"})
@RequiredArgsConstructor
public class AuditLogConsumer {

    private final AuditLogMongoRepository auditLogMongoRepository;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT",
            autoCreateTopics = "true"
    )
    @KafkaListener(
            topics = "banking.transactions",
            groupId = "audit-log-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        log.debug("AuditLogConsumer received event [transactionId={}]", event.transactionId());

        if (auditLogMongoRepository.existsByTransactionId(event.transactionId())) {
            log.info("Duplicate audit event skipped [transactionId={}]", event.transactionId());
            return;
        }

        AuditLogDocument auditLog = AuditLogDocument.builder()
                .transactionId(event.transactionId())
                .accountId(event.sourceAccountId() != null ? event.sourceAccountId() : event.targetAccountId())
                .eventType(event.type())
                .amount(event.amount())
                .currency(event.currency())
                .status(event.status())
                .metadata(buildMetadata(event))
                .timestamp(Instant.now())
                .build();

        auditLogMongoRepository.save(auditLog);
        log.info("Audit log persisted [transactionId={}, type={}]", event.transactionId(), event.type());
    }

    private String buildMetadata(TransactionEvent event) {
        if (event.sourceAccountId() != null && event.targetAccountId() != null) {
            return String.format("transfer: %s -> %s", event.sourceAccountId(), event.targetAccountId());
        }
        return event.type().toLowerCase();
    }
}
