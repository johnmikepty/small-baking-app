package com.smallbankapp.banking.application.usecase.auth.command;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.UserRepository;
import com.smallbankapp.banking.domain.exception.UserAlreadyExistsException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock UserRepository userRepository;
    @Mock AccountRepository accountRepository;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks RegisterUserHandler handler;

    private RegisterUserCommand command;

    @BeforeEach
    void setUp() {
        command = new RegisterUserCommand("test@email.com", "Password1!", "Test User");
    }

    @Test
    void handle_newUser_returnsResultWithAccountNumber() {
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(command.password())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserResult result = handler.handle(command);

        assertThat(result.email()).isEqualTo(command.email());
        assertThat(result.fullName()).isEqualTo(command.fullName());
        assertThat(result.accountNumber()).isNotBlank();
        verify(userRepository).save(any(User.class));
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void handle_duplicateEmail_throwsUserAlreadyExistsException() {
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        assertThatThrownBy(() -> handler.handle(command))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
        verify(accountRepository, never()).save(any());
    }
}
