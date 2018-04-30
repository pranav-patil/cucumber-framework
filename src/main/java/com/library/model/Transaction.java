package com.library.model;

import java.math.BigDecimal;

public class Transaction {

	private String accountNumber;
	private String recipientAccount;
	private BigDecimal amount;
	private String currencyCode;

	public Transaction() {
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getRecipientAccount() {
		return recipientAccount;
	}

	public void setRecipientAccount(String recipientAccount) {
		this.recipientAccount = recipientAccount;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	@Override
	public String toString() {
		return "Customer [accountNumber=" + accountNumber
				+ ", recipientAccount=" + recipientAccount
				+ ", amount=" + amount
				+ ", currencyCode=" + currencyCode
				+ "]";
	}
}