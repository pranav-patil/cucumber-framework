package com.library.validation;

import com.library.model.Customer;
import com.library.response.MessageCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class CustomerValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return Customer.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {

		if (errors.hasErrors()) {
			return;
		}

		Customer customer = (Customer) target;

		if(StringUtils.isBlank(customer.getName())) {
			errors.reject(MessageCode.UNKNOWN_ERROR.name(), "Customer name is blank");
		}

		//ValidationUtils.rejectIfEmpty(errors, "name", "machinePaymentsPageBean.cartId.empty", "Name field is empty");
	}
}