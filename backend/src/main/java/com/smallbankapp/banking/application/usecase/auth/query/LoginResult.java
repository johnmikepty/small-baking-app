package com.smallbankapp.banking.application.usecase.auth.query;

public record LoginResult(
        String token,
        String email,
        String fullName
) {}
