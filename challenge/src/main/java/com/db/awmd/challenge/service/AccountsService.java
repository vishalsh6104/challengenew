package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.AccountDoesNotExistsException;
import com.db.awmd.challenge.exception.InsufficientFunds;
import com.db.awmd.challenge.repository.AccountsRepository;

import lombok.Getter;

@Service
public class AccountsService {

	private static final Logger log = LoggerFactory.getLogger(AccountsService.class);

	@Getter
	private final AccountsRepository accountsRepository;

	@Getter
	private final NotificationService notificationService;

	@Autowired
	public AccountsService(AccountsRepository accountsRepository, NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService = notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	
	public void transferAmount(TransferBalanceRequest transferBalanceRequest) throws InsufficientFunds, Exception {
		BigDecimal amount = transferBalanceRequest.getAmount();
		Account accFrom = accountsRepository.getAccount(transferBalanceRequest.getFromAccountNumber());
		Account accTo = accountsRepository.getAccount(transferBalanceRequest.getToAccountNumber());

		if (accFrom == null) {
			throw new AccountDoesNotExistsException(
					"Account From id " + transferBalanceRequest.getFromAccountNumber() + " does not exists!");
		}
		if (accTo == null) {
			throw new AccountDoesNotExistsException(
					"Account To id " + transferBalanceRequest.getToAccountNumber() + " does not exists!");
		}

		Random rand = new Random();
		Lock fromAccountTransferLock = accFrom.transferLock;
		Lock toAccountTransferLock = accTo.transferLock;

		if (fromAccountTransferLock.tryLock()) {
			accFrom.lock.writeLock();
			if (toAccountTransferLock.tryLock()) {
				try {
					if (accFrom.getBalance().compareTo(amount) == 0 || accFrom.getBalance().compareTo(amount) == 1) {
						Lock accountWriteFromLock = accFrom.lock.writeLock();
						accountWriteFromLock.lock();
						Lock accountWriteToLock = accTo.lock.writeLock();
						accountWriteToLock.lock();
						this.accountsRepository.withdraw(accFrom, amount);
						this.accountsRepository.deposit(accTo, amount);
						accountWriteFromLock.unlock();
						accountWriteToLock.unlock();
						log.info("Funds successfully transfered.");
						notificationService.notifyAboutTransfer(accFrom, "Funds successfully transfered.");

					} else {
						log.info("Insufficient funds");
						throw new InsufficientFunds("Account From id " + transferBalanceRequest.getFromAccountNumber()
								+ " is having insufficient funds!");
					}

				} catch (InsufficientFunds e) {
					log.info("Exception occur while transfer amount" + e.getMessage());
					throw e;

				} catch (Exception e) {
					log.info("Exception occur while transfer amount" + e.getMessage());
					throw e;
				} finally {
					toAccountTransferLock.unlock();
					fromAccountTransferLock.unlock();
					Thread.sleep(rand.nextInt(1001));

				}
			}
		}
	}

	public Account getAccount(String accountId) {
		return this.accountsRepository.getAccount(accountId);
	}

	public AccountsRepository getAccountsRepository() {
		return accountsRepository;
	}

	public NotificationService getNotificationService() {
		return notificationService;
	}
}
