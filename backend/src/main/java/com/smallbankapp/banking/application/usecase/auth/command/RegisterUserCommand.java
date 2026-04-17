package com.smallbankapp.banking.application.usecase.auth.command;

import com.smallbankapp.banking.application.mediator.Request;

public record RegisterUserCommand(
        String email,
        String password,
        String fullName
) implements Request<RegisterUserResult> {}
