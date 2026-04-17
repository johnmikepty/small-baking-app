package com.smallbankapp.banking.infrastructure.persistence.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Denormalized transaction record optimized for fast reads.
 * Written after every successful SQL transaction via Kafka consumer.
 */
@Document(collection = "transaction_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionHistoryDocument {

    @Id
    private String id;

    @Indexed
    @Field("transaction_id")
    private UUID transactionId;

    @Indexed
    @Field("account_id")
    private UUID accountId;

    @Field("counterpart_account_id")
    private UUID counterpartAccountId;

    @Field("transaction_type")
    private String transactionType;

    @Field("amount")
    private BigDecimal amount;

    @Field("currency")
    private String currency;

    @Field("description")
    private String description;

    @Field("status")
    private String status;

    @Indexed
    @Field("created_at")
    private Instant createdAt;
}
