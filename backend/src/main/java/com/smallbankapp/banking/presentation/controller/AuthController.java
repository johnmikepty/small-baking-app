package com.smallbankapp.banking.presentation.controller;

import com.smallbankapp.banking.application.mediator.Mediator;
import com.smallbankapp.banking.application.usecase.auth.command.RegisterUserCommand;
import com.smallbankapp.banking.application.usecase.auth.command.RegisterUserResult;
import com.smallbankapp.banking.application.usecase.auth.query.LoginQuery;
import com.smallbankapp.banking.application.usecase.auth.query.LoginResult;
import com.smallbankapp.banking.presentation.dto.request.LoginRequest;
import com.smallbankapp.banking.presentation.dto.request.RegisterRequest;
import com.smallbankapp.banking.presentation.dto.response.LoginResponse;
import com.smallbankapp.banking.presentation.dto.response.RegisterResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, login and logout")
public class AuthController {

    private final Mediator mediator;

    @PostMapping("/register")
    @Operation(summary = "Register a new user and create a linked bank account")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterUserResult result = mediator.send(
                new RegisterUserCommand(request.email(), request.password(), request.fullName()));

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RegisterResponse(
                        result.userId(),
                        result.email(),
                        result.fullName(),
                        result.accountId(),
                        result.accountNumber()));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT token")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = mediator.send(new LoginQuery(request.email(), request.password()));
        return ResponseEntity.ok(new LoginResponse(result.token(), result.email(), result.fullName()));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (client-side token invalidation)")
    public ResponseEntity<Map<String, String>> logout() {
        // Stateless JWT — token invalidation is handled client-side
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
