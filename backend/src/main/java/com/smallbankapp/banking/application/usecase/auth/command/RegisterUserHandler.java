package com.smallbankapp.banking.application.usecase.auth.command;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.UserRepository;
import com.smallbankapp.banking.domain.exception.UserAlreadyExistsException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.User;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterUserHandler implements RequestHandler<RegisterUserCommand, RegisterUserResult> {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RegisterUserResult handle(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        // Create and persist user
        User user = User.create(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.fullName()
        );
        user = userRepository.save(user);

        // Create account automatically linked to user
        Account account = Account.create(user.getId(), AccountType.SAVINGS, "USD");
        account = accountRepository.save(account);

        return new RegisterUserResult(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                account.getId(),
                account.getAccountNumber().getValue()
        );
    }
}
