package com.library.hibernate.dao;


import com.library.hibernate.domain.Account;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.Optional;

public interface AccountDAO extends Repository<Account, Long> {

    Collection<Account> findAll();
    Optional<Account> findByAccountId(String accountId);
    Optional<Account> findById(Long id);
    Integer countByAccountId(String accountId);
    Account save(Account account);
    void deleteAccountById(Long id);
}
