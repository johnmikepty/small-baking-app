package com.smallbankapp.banking.application.mediator;

/**
 * Handler for a specific Request type.
 *
 * @param <C> the Request (Command or Query)
 * @param <R> the return type
 */
public interface RequestHandler<C extends Request<R>, R> {
    R handle(C request);
}
