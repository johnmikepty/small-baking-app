package com.smallbankapp.banking.infrastructure.messaging.sqs.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smallbankapp.banking.application.port.out.TransactionEventPublisher;
import com.smallbankapp.banking.domain.model.Transaction;
import com.smallbankapp.banking.infrastructure.messaging.kafka.event.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * SQS adapter implementing TransactionEventPublisher.
 * Active on profiles: aws, localstack.
 * Publishes TransactionEvent as JSON to the banking-transactions SQS queue.
 */
@Slf4j
@Component
@Profile({"aws", "localstack"})
@RequiredArgsConstructor
public class SqsTransactionProducer implements TransactionEventPublisher {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;

    @Value("${sqs.queue-url}")
    private String queueUrl;

    @Override
    public void publish(Transaction transaction) {
        try {
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

            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(messageBody)
                    .build();

            var result = sqsClient.sendMessage(request);
            log.debug("SQS message sent [transactionId={}, messageId={}]",
                    transaction.getId(), result.messageId());

        } catch (Exception e) {
            log.error("Failed to publish SQS event [transactionId={}]: {}",
                    transaction.getId(), e.getMessage(), e);
        }
    }
}
