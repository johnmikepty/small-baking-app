package com.smallbankapp.banking.infrastructure.messaging.kafka.consumer;

import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import com.smallbankapp.banking.infrastructure.persistence.mongo.document.TransactionHistoryDocument;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.TransactionHistoryMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kafka consumer for transaction history.
 * Active on profiles: dev, docker, test.
 * On aws/localstack profiles, SqsAuditLogConsumer takes over.
 */
@Slf4j
@Component
@Profile({"dev", "docker", "test"})
@RequiredArgsConstructor
public class TransactionHistoryConsumer {

    private final TransactionHistoryMongoRepository transactionHistoryMongoRepository;

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = ".DLT",
            autoCreateTopics = "true"
    )
    @KafkaListener(
            topics = "banking.transactions",
            groupId = "transaction-history-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(TransactionEvent event) {
        log.debug("TransactionHistoryConsumer received event [transactionId={}]", event.transactionId());

        if (transactionHistoryMongoRepository.existsByTransactionId(event.transactionId())) {
            log.info("Duplicate history event skipped [transactionId={}]", event.transactionId());
            return;
        }

        UUID primaryAccountId = event.sourceAccountId() != null
                ? event.sourceAccountId() : event.targetAccountId();

        UUID counterpart = (event.sourceAccountId() != null && event.targetAccountId() != null)
                ? event.targetAccountId() : null;

        TransactionHistoryDocument doc = TransactionHistoryDocument.builder()
                .transactionId(event.transactionId())
                .accountId(primaryAccountId)
                .counterpartAccountId(counterpart)
                .transactionType(event.type())
                .amount(event.amount())
                .currency(event.currency())
                .description(null)
                .status(event.status())
                .createdAt(event.timestamp())
                .build();

        transactionHistoryMongoRepository.save(doc);
        log.info("Transaction history persisted [transactionId={}, type={}]",
                event.transactionId(), event.type());
    }
}
