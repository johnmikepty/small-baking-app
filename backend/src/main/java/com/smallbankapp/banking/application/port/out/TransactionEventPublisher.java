package com.smallbankapp.banking.application.port.out;

import com.smallbankapp.banking.domain.model.Transaction;

/**
 * Port for publishing transaction events to the message broker.
 */
public interface TransactionEventPublisher {
    void publish(Transaction transaction);
}
