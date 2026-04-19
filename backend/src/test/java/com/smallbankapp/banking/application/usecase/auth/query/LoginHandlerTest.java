package com.smallbankapp.banking.application.usecase.auth.query;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.UserRepository;
import com.smallbankapp.banking.domain.exception.InvalidCredentialsException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.User;
import com.smallbankapp.banking.domain.valueobject.AccountNumber;
import com.smallbankapp.banking.domain.valueobject.AccountStatus;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import com.smallbankapp.banking.domain.valueobject.Money;
import com.smallbankapp.banking.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginHandlerTest {

    @Mock UserRepository userRepository;
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;

    @InjectMocks LoginHandler handler;

    private User mockUser;
    private Account mockAccount;
    private LoginQuery query;

    @BeforeEach
    void setUp() {
        mockUser = new User(
                UUID.randomUUID(),
                "test@email.com",
                "hashed_password",
                "Test User",
                Instant.now(),
                Instant.now()
        );
        mockAccount = new Account(
                UUID.randomUUID(),
                mockUser.getId(),
                AccountNumber.of("1234567890"),
                AccountType.SAVINGS,
                Money.of(0.0, "USD"),
                AccountStatus.ACTIVE,
                Instant.now(),
                Instant.now()
        );
        query = new LoginQuery("test@email.com", "Password1!");
    }

    @Test
    void handle_validCredentials_returnsTokenAndUserInfo() {
        when(userRepository.findByEmail(query.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(query.password(), mockUser.getPasswordHash())).thenReturn(true);
        when(accountRepository.findByUserId(mockUser.getId())).thenReturn(Optional.of(mockAccount));
        when(jwtService.generateToken(mockUser.getId(), mockUser.getEmail(), mockAccount.getId()))
                .thenReturn("jwt-token");

        LoginResult result = handler.handle(query);

        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.email()).isEqualTo(mockUser.getEmail());
        assertThat(result.fullName()).isEqualTo(mockUser.getFullName());
    }

    @Test
    void handle_userNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail(query.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void handle_wrongPassword_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail(query.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(query.password(), mockUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void handle_accountNotFound_throwsInvalidCredentialsException() {
        when(userRepository.findByEmail(query.email())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(query.password(), mockUser.getPasswordHash())).thenReturn(true);
        when(accountRepository.findByUserId(mockUser.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(query))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), any(), any());
    }
}
