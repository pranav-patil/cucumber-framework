package com.library.hibernate.dao;

import com.library.hibernate.domain.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionDAO extends JpaRepository<TransactionRecord, Long> {

    Optional<TransactionRecord> findByAccountId(String accountId);
    Optional<TransactionRecord> findById(Long id);
    TransactionRecord save(TransactionRecord account);
}
