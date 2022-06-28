package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public interface NotificationService {
  void notifyAboutTransfer(Account account, String transferDescription);
}
