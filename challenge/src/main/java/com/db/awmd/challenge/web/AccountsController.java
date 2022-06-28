package com.db.awmd.challenge.web;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.TransferBalanceRequest;
import com.db.awmd.challenge.exception.AccountDoesNotExistsException;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InsufficientFunds;
import com.db.awmd.challenge.service.AccountsService;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/accounts")
@Slf4j
public class AccountsController {

	private static final Logger log = LoggerFactory.getLogger(AccountsController.class);

	private final AccountsService accountsService;

	@Autowired
	public AccountsController(AccountsService accountsService) {
		this.accountsService = accountsService;
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> createAccount(@RequestBody @Valid Account account) {
		log.info("Creating account {}", account);

		try {
			this.accountsService.createAccount(account);
		} catch (DuplicateAccountIdException daie) {
			return new ResponseEntity<>(daie.getMessage(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.CREATED);
	}

	@PostMapping(path = "/{transfer}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody ResponseEntity<Object> transferAmount(
			@RequestBody @Valid TransferBalanceRequest transferRequest) {
		log.info("Transfer amount from {} to {}", transferRequest.getFromAccountNumber(),
				transferRequest.getToAccountNumber());

		try {
			this.accountsService.transferAmount(transferRequest);
			return new ResponseEntity<>(HttpStatus.CREATED);
		} catch (AccountDoesNotExistsException acc) {
			return new ResponseEntity<>(acc.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (InsufficientFunds insu) {
			return new ResponseEntity<>(insu.getMessage(), HttpStatus.BAD_REQUEST);
		}
		 catch (Exception insu) {
				return new ResponseEntity<>(insu.getMessage(), HttpStatus.EXPECTATION_FAILED);
			}

	}

	@GetMapping(path = "/{accountId}")
	public Account getAccount(@PathVariable String accountId) {
		log.info("Retrieving account for id {}", accountId);
		return this.accountsService.getAccount(accountId);
	}

	

}
