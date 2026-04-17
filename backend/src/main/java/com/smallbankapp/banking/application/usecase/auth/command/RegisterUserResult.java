package com.smallbankapp.banking.application.usecase.auth.command;

import java.util.UUID;

public record RegisterUserResult(
        UUID userId,
        String email,
        String fullName,
        UUID accountId,
        String accountNumber
) {}
