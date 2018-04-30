package com.library.validation;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

@Component
@Aspect
public class RequestBodyValidatorAspect implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Before("@annotation(validate)")
	public void validate(JoinPoint joinPoint, Validate validate) {
		Object target = null;
		BindingResult result = null;

		// Get the class type and validators' id/names
		Class<?> type = validate.type();
		String[] validators = validate.validators();

		// Get the arguments
		Object[] args = joinPoint.getArgs();

		// Check arguments array and retrieve the target bean and the BindingResult object
		for (int i = 0; i < args.length; i++) {
			if (args[i] instanceof BindingResult && result == null) {
				result = (BindingResult) args[i];
			} else if (type.isInstance(args[i])) {
				target = args[i];
			} else if (target != null && result != null)
				break;
		}

		if (target != null) {
			if (result == null) {
				result = new BeanPropertyBindingResult(target, type.getName());
			}

			for (String validator : validators) {
				// Get the validator bean from application context and validate the target bean
				Validator validatorInstance = getValidator(validator);
				validatorInstance.validate(target, result);

				if (result.hasErrors()) {
					throw new ValidationException(result);
				}
			}
		} else {
			throw new IllegalArgumentException(String.format("@Validate type argument '%s' not present for class method %s.%s", type.getSimpleName(),
					joinPoint.getSignature().getDeclaringType().getSimpleName(), joinPoint.getSignature().getName()));
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		if (this.applicationContext == null) {
			this.applicationContext = applicationContext;
		}
	}

	private Validator getValidator(String validator) {
		return (Validator) applicationContext.getBean(validator);
	}
}