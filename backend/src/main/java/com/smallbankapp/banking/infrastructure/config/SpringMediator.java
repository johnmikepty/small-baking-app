package com.smallbankapp.banking.infrastructure.config;

import com.smallbankapp.banking.application.mediator.Mediator;
import com.smallbankapp.banking.application.mediator.Request;
import com.smallbankapp.banking.application.mediator.RequestHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring-managed Mediator implementation.
 * On startup it discovers all RequestHandler beans and builds
 * a Command/Query → Handler registry.
 */
@Component
public class SpringMediator implements Mediator {

    private final Map<Class<?>, RequestHandler<?, ?>> handlers = new HashMap<>();

    @SuppressWarnings({"rawtypes", "unchecked"})
    public SpringMediator(ApplicationContext context) {
        Map<String, RequestHandler> beans = context.getBeansOfType(RequestHandler.class);
        for (RequestHandler<?, ?> handler : beans.values()) {
            Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(
                    handler.getClass(), RequestHandler.class);
            if (typeArgs != null && typeArgs.length > 0) {
                handlers.put(typeArgs[0], handler);
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <R> R send(Request<R> request) {
        RequestHandler handler = handlers.get(request.getClass());
        if (handler == null) {
            throw new IllegalStateException(
                    "No handler registered for: " + request.getClass().getSimpleName());
        }
        return (R) handler.handle(request);
    }
}
