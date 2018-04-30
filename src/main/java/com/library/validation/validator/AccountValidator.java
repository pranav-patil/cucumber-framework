package com.library.validation.validator;

import com.library.model.Account;
import com.library.model.Transaction;
import com.library.response.MessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;

@Component
public class AccountValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Account.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		if (errors.hasErrors()) {
			return;
		}

		Account account = (Account) target;

		if(StringUtils.isBlank(account.getCustomerId())) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Customer Id cannot be empty");
		}

		if(StringUtils.isBlank(account.getBranchId())) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Branch Id cannot be empty");
		}

		if(BigDecimal.ZERO.compareTo(account.getBalance()) > -1) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Initial account balance amount cannot be negative or zero");
		}
	}
}