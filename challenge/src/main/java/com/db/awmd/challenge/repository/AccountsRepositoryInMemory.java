package com.db.awmd.challenge.repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

@Repository
public class AccountsRepositoryInMemory implements AccountsRepository {

	private static final Logger log = LoggerFactory.getLogger(AccountsRepositoryInMemory.class);

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account) throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(), account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id " + account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) {
		return accounts.get(accountId);
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}
	
	@Override
	public void withdraw(Account account, BigDecimal amount) {
		account.setBalance(account.getBalance().subtract(amount));
		accounts.putIfAbsent(account.getAccountId(), account);
	}
	
	@Override
	public void deposit(Account account, BigDecimal amount) {
		account.setBalance(account.getBalance().add(amount));
		accounts.putIfAbsent(account.getAccountId(), account);
	}
	
}
