package com.smallbankapp.banking.infrastructure.messaging.kafka.producer;

import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka adapter that implements the TransactionEventPublisher domain port.
 * Active on profiles: dev, docker, test.
 * On aws/localstack profiles, SqsTransactionProducer takes over.
 */
@Slf4j
@Component
@Profile({"dev", "docker", "test"})
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
