package com.smallbankapp.banking.infrastructure.persistence.mongo.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable audit log entry for all system operations.
 * Written by the Kafka AuditLogConsumer. Never modified after creation.
 */
@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("transaction_id")
    private UUID transactionId;

    @Indexed
    @Field("account_id")
    private UUID accountId;

    @Field("event_type")
    private String eventType;

    @Field("amount")
    private java.math.BigDecimal amount;

    @Field("currency")
    private String currency;

    @Field("status")
    private String status;

    @Field("metadata")
    private String metadata;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;
}
