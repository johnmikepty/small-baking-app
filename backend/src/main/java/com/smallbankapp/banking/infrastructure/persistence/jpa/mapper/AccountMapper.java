package com.smallbankapp.banking.infrastructure.persistence.jpa.mapper;

import com.smallbankapp.banking.domain.model.Account;
import com.smallbankapp.banking.domain.valueobject.AccountNumber;
import com.smallbankapp.banking.domain.valueobject.AccountStatus;
import com.smallbankapp.banking.domain.valueobject.AccountType;
import com.smallbankapp.banking.domain.valueobject.Money;
import com.smallbankapp.banking.infrastructure.persistence.jpa.entity.AccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
                entity.getId(),
                entity.getUserId(),
                AccountNumber.of(entity.getAccountNumber()),
                AccountType.valueOf(entity.getAccountType()),
                Money.of(entity.getBalance(), entity.getCurrency()),
                AccountStatus.valueOf(entity.getStatus()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public AccountJpaEntity toEntity(Account domain) {
        return AccountJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .accountNumber(domain.getAccountNumber().getValue())
                .accountType(domain.getAccountType().name())
                .balance(domain.getBalance().getAmount())
                .currency(domain.getBalance().getCurrency())
                .status(domain.getStatus().name())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
