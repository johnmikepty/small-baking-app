package com.smallbankapp.banking.infrastructure.messaging.kafka.producer;

import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter that implements the TransactionEventPublisher domain port.
 * Publishes TransactionEvent to the "banking.transactions" topic.
 * Spring Kafka auto-configures the dead-letter topic on deserialization failure.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaTransactionProducer implements TransactionEventPublisher {

    static final String TOPIC = "banking.transactions";

    private final KafkaTemplate<String, TransactionEvent> kafkaTemplate;

    @Override
    public void publish(Transaction transaction) {
        TransactionEvent event = TransactionEvent.of(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getTargetAccountId(),
                transaction.getType().name(),
                transaction.getAmount().getAmount(),
                transaction.getAmount().getCurrency(),
                transaction.getStatus().name(),
                transaction.getCreatedAt()
        );

        // Partition key: accountId (source or target) for ordering per account
        String key = resolveKey(transaction);

        CompletableFuture<SendResult<String, TransactionEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish TransactionEvent [id={}]: {}",
                        transaction.getId(), ex.getMessage(), ex);
            } else {
                log.debug("TransactionEvent published [id={}, partition={}, offset={}]",
                        transaction.getId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    private String resolveKey(Transaction t) {
        if (t.getSourceAccountId() != null) return t.getSourceAccountId().toString();
        if (t.getTargetAccountId() != null) return t.getTargetAccountId().toString();
        return t.getId().toString();
    }
}
