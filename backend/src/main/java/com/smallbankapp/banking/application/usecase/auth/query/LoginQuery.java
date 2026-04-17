package com.smallbankapp.banking.application.usecase.auth.query;

import com.smallbankapp.banking.application.mediator.Request;

public record LoginQuery(
        String email,
        String password
) implements Request<LoginResult> {}
