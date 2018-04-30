package com.library.validation.validator;

import com.library.model.Transaction;
import com.library.response.MessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class TransactionValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Transaction.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		if (errors.hasErrors()) {
			return;
		}

		Transaction transaction = (Transaction) target;

		if(StringUtils.isBlank(transaction.getAccountNumber())) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Account number cannot be empty");
		}

		if(BigDecimal.ZERO.compareTo(transaction.getAmount()) > -1) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Amount cannot be negative or zero");
		}
	}
}