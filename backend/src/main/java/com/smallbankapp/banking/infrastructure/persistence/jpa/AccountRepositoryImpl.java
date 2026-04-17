package com.smallbankapp.banking.infrastructure.persistence.jpa;

import com.smallbankapp.banking.application.port.out.AccountRepository;
import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.infrastructure.persistence.jpa.mapper.AccountMapper;
import com.smallbankapp.banking.infrastructure.persistence.jpa.repository.AccountJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class AccountRepositoryImpl implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    @Override
    public Account save(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(account)));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findByAccountNumber(accountNumber).map(mapper::toDomain);
    }

    @Override
    public Optional<Account> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId).map(mapper::toDomain);
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) {
        return jpaRepository.existsByAccountNumber(accountNumber);
    }
}
