package com.smallbankapp.banking.application.usecase.account.query;

import com.smallbankapp.banking.application.mediator.Request;

import java.util.UUID;

public record GetAccountQuery(UUID accountId) implements Request<GetAccountResult> {}
