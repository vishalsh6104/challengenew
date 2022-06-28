package com.db.awmd.challenge.repository;

import java.math.BigDecimal;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;

public interface AccountsRepository {

	
	void createAccount(Account account) throws DuplicateAccountIdException;

	Account getAccount(String accountId);
	
	void withdraw(Account account,BigDecimal amount) throws DuplicateAccountIdException;
	
	void deposit(Account account,BigDecimal amount) throws DuplicateAccountIdException;

	void clearAccounts();
	
	
}
