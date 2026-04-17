package com.smallbankapp.banking.presentation.dto.response;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        String fullName,
        UUID accountId,
        String accountNumber
) {}
