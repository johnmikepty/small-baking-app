package com.smallbankapp.banking.application.mediator;

/**
 * Central dispatcher: routes a Request to its registered Handler.
 */
public interface Mediator {
    <R> R send(Request<R> request);
}
