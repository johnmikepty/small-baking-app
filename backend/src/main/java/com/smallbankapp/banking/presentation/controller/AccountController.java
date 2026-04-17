package com.smallbankapp.banking.presentation.controller;

import com.smallbankapp.banking.application.mediator.Mediator;
import com.smallbankapp.banking.application.usecase.account.query.GetAccountQuery;
import com.smallbankapp.banking.application.usecase.account.query.GetAccountResult;
import com.smallbankapp.banking.application.usecase.account.query.GetBalanceQuery;
import com.smallbankapp.banking.application.usecase.account.query.GetBalanceResult;
import com.smallbankapp.banking.presentation.dto.response.AccountResponse;
import com.smallbankapp.banking.presentation.dto.response.BalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account information and balance")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final Mediator mediator;

    @GetMapping("/{accountId}")
    @Operation(summary = "Get account details by ID")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID accountId) {
        GetAccountResult result = mediator.send(new GetAccountQuery(accountId));
        return ResponseEntity.ok(new AccountResponse(
                result.accountId(),
                result.userId(),
                result.accountNumber(),
                result.accountType(),
                result.balance(),
                result.currency(),
                result.status(),
                result.createdAt()
        ));
    }

    @GetMapping("/{accountId}/balance")
    @Operation(summary = "Get current balance for an account")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID accountId) {
        GetBalanceResult result = mediator.send(new GetBalanceQuery(accountId));
        return ResponseEntity.ok(new BalanceResponse(
                result.accountId(),
                result.accountNumber(),
                result.balance(),
                result.currency()
        ));
    }
}
