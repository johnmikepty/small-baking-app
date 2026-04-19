package com.smallbankapp.banking.application.usecase.auth.query;

import com.smallbankapp.banking.application.mediator.RequestHandler;
import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.UserRepository;
import com.smallbankapp.banking.domain.exception.InvalidCredentialsException;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.User;
import com.smallbankapp.banking.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginHandler implements RequestHandler<LoginQuery, LoginResult> {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public LoginResult handle(LoginQuery query) {
        User user = userRepository.findByEmail(query.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(query.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        // Resolve the account linked to this user so we can embed accountId in the JWT
        Account account = accountRepository.findByUserId(user.getId())
                .orElseThrow(InvalidCredentialsException::new);

        String token = jwtService.generateToken(user.getId(), user.getEmail(), account.getId());

        return new LoginResult(token, user.getEmail(), user.getFullName());
    }
}
