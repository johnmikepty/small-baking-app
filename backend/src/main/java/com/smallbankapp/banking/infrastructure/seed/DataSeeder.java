package com.smallbankapp.banking.infrastructure.seed;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.application.port.out.UserRepository;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.model.User;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Loads mock data from mock-data.json on startup.
 * Active only on 'dev' profile.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail("ihernandez@email.com")) {
            log.info("DataSeeder: mock data already loaded, skipping.");
            return;
        }

        log.info("DataSeeder: loading mock data...");

        ClassPathResource resource = new ClassPathResource("mock-data.json");
        MockData mockData = objectMapper.readValue(resource.getInputStream(), MockData.class);

        for (MockUser mockUser : mockData.users()) {
            if (userRepository.existsByEmail(mockUser.email())) continue;

            User user = new User(
                    UUID.fromString(mockUser.id()),
                    mockUser.email(),
                    passwordEncoder.encode(mockUser.password()),
                    mockUser.fullName(),
                    Instant.parse(mockUser.createdAt()),
                    Instant.parse(mockUser.createdAt())
            );
            userRepository.save(user);

            Account account = Account.create(user.getId(), AccountType.SAVINGS, "USD");
            accountRepository.save(account);
        }

        log.info("DataSeeder: mock data loaded successfully.");
    }

    // ── Internal DTOs for JSON deserialization ───────────────

    record MockData(List<MockUser> users) {}

    record MockUser(
            String id,
            String email,
            String password,
            @JsonProperty("full_name") String fullName,
            @JsonProperty("created_at") String createdAt
    ) {}
}
