package com.smallbankapp.banking.presentation.dto.response;

public record LoginResponse(
        String token,
        String email,
        String fullName
) {}
