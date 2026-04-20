package com.smallbankapp.banking.infrastructure.messaging.sqs.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import com.smallbankapp.banking.infrastructure.persistence.mongo.document.AuditLogDocument;
import com.smallbankapp.banking.infrastructure.persistence.mongo.document.TransactionHistoryDocument;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.AuditLogMongoRepository;
import com.smallbankapp.banking.infrastructure.persistence.mongo.repository.TransactionHistoryMongoRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * SQS consumer — polls banking-transactions queue and writes to MongoDB.
 * Handles both audit_logs and transaction_history collections.
 * Active on profiles: aws, localstack.
 * Idempotent: skips duplicate transactionIds.
 */
@Slf4j
@Component
@Profile({"aws", "localstack"})
@RequiredArgsConstructor
public class SqsAuditLogConsumer {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final AuditLogMongoRepository auditLogMongoRepository;
    private final TransactionHistoryMongoRepository transactionHistoryMongoRepository;

    @Value("${sqs.queue-url}")
    private String queueUrl;

    /**
     * Polls SQS every 5 seconds for new transaction events.
     */
    @Scheduled(fixedDelay = 5000)
    public void poll() {
        try {
            ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .maxNumberOfMessages(10)
                    .waitTimeSeconds(5) // long polling
                    .build();

            List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

            for (Message message : messages) {
                try {
                    processMessage(message);
                    // Delete message after successful processing
                    sqsClient.deleteMessage(DeleteMessageRequest.builder()
                            .queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle())
                            .build());
                } catch (Exception e) {
                    log.error("Failed to process SQS message [messageId={}]: {}",
                            message.messageId(), e.getMessage(), e);
                    // Message will reappear after visibility timeout → DLT after max retries
                }
            }
        } catch (Exception e) {
            log.error("SQS polling error: {}", e.getMessage(), e);
        }
    }

    private void processMessage(Message message) throws Exception {
        TransactionEvent event = objectMapper.readValue(message.body(), TransactionEvent.class);
        log.debug("SQS event received [transactionId={}]", event.transactionId());

        persistAuditLog(event);
        persistTransactionHistory(event);
    }

    private void persistAuditLog(TransactionEvent event) {
        if (auditLogMongoRepository.existsByTransactionId(event.transactionId())) {
            log.info("Duplicate audit event skipped [transactionId={}]", event.transactionId());
            return;
        }

        UUID accountId = event.sourceAccountId() != null
                ? event.sourceAccountId() : event.targetAccountId();

        AuditLogDocument doc = AuditLogDocument.builder()
                .transactionId(event.transactionId())
                .accountId(accountId)
                .eventType(event.type())
                .amount(event.amount())
                .currency(event.currency())
                .status(event.status())
                .metadata(buildMetadata(event))
                .timestamp(Instant.now())
                .build();

        auditLogMongoRepository.save(doc);
        log.info("Audit log persisted [transactionId={}]", event.transactionId());
    }

    private void persistTransactionHistory(TransactionEvent event) {
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
                .status(event.status())
                .createdAt(event.timestamp())
                .build();

        transactionHistoryMongoRepository.save(doc);
        log.info("Transaction history persisted [transactionId={}]", event.transactionId());
    }

    private String buildMetadata(TransactionEvent event) {
        if (event.sourceAccountId() != null && event.targetAccountId() != null) {
            return String.format("transfer: %s -> %s", event.sourceAccountId(), event.targetAccountId());
        }
        return event.type().toLowerCase();
    }
}
