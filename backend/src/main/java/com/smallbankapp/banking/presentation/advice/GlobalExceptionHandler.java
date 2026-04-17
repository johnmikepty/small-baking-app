package com.smallbankapp.banking.presentation.advice;

import com.smallbankapp.banking.domain.exception.AccountNotFoundException;
import com.smallbankapp.banking.domain.exception.DomainException;
import com.smallbankapp.banking.domain.exception.InsufficientFundsException;
import com.smallbankapp.banking.domain.exception.InvalidCredentialsException;
import com.smallbankapp.banking.domain.exception.UserAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Centralized exception → HTTP response mapping.
 * Uses RFC-7807 ProblemDetail (Spring 6 native).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    ProblemDetail handleUserAlreadyExists(UserAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage(), "user-already-exists");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage(), "invalid-credentials");
    }

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail handleAccountNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage(), "account-not-found");
    }

    @ExceptionHandler(InsufficientFundsException.class)
    ProblemDetail handleInsufficientFunds(InsufficientFundsException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "insufficient-funds");
    }

    @ExceptionHandler(DomainException.class)
    ProblemDetail handleDomain(DomainException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage(), "domain-error");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed", "validation-error");
        pd.setProperty("errors", errors);
        return pd;
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleGeneric(Exception ex) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "internal-error");
    }

    private ProblemDetail problem(HttpStatus status, String detail, String errorCode) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://banking.smallbankapp.com/errors/" + errorCode));
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
