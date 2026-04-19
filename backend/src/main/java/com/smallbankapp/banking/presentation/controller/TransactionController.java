package com.smallbankapp.banking.presentation.controller;

import com.smallbankapp.banking.application.mediator.Mediator;
import com.smallbankapp.banking.application.usecase.transaction.command.DepositCommand;
import com.smallbankapp.banking.application.usecase.transaction.command.TransactionResult;
import com.smallbankapp.banking.application.usecase.transaction.command.TransferCommand;
import com.smallbankapp.banking.application.usecase.transaction.command.WithdrawCommand;
import com.smallbankapp.banking.application.usecase.transaction.query.GetTransactionHistoryQuery;
import com.smallbankapp.banking.application.usecase.transaction.query.TransactionHistoryResult;
import com.smallbankapp.banking.presentation.dto.request.DepositRequest;
import com.smallbankapp.banking.presentation.dto.request.TransferRequest;
import com.smallbankapp.banking.presentation.dto.request.WithdrawRequest;
import com.smallbankapp.banking.presentation.dto.response.TransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Deposit, withdraw, transfer and history")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final Mediator mediator;

    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds into an account")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        TransactionResult result = mediator.send(new DepositCommand(
                request.idempotencyKey(),
                request.accountId(),
                request.amount(),
                request.currency(),
                request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from an account")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawRequest request) {
        TransactionResult result = mediator.send(new WithdrawCommand(
                request.idempotencyKey(),
                request.accountId(),
                request.amount(),
                request.currency(),
                request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransactionResult result = mediator.send(new TransferCommand(
                request.idempotencyKey(),
                request.sourceAccountId(),
                request.targetAccountId(),
                request.amount(),
                request.currency(),
                request.description()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping("/history")
    @Operation(summary = "Get paginated transaction history for an account")
    public ResponseEntity<Page<TransactionHistoryResult>> history(
            @RequestParam UUID accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<TransactionHistoryResult> results = mediator.send(
                new GetTransactionHistoryQuery(accountId, page, size));
        return ResponseEntity.ok(results);
    }

    private TransactionResponse toResponse(TransactionResult result) {
        return new TransactionResponse(
                result.transactionId(),
                result.type(),
                result.amount(),
                result.currency(),
                result.status(),
                result.createdAt()
        );
    }
}
