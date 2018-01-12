package com.emprovise.validation;

import com.emprovise.response.MessageCode;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;

public class ValidationException extends ServiceException {

	private static final long serialVersionUID = -648442549020814383L;
	private final BindingResult bindingResult;
	
	public ValidationException(BindingResult bindingResult) {
		super(MessageCode.UNKNOWN_ERROR);
		Assert.notNull(bindingResult, "BindingResult must not be null");
		this.bindingResult = bindingResult;
	}
	
	public final BindingResult getBindingResult() {
		return this.bindingResult;
	}

	public String getObjectName() {
		return this.bindingResult.getObjectName();
	}

	public void setNestedPath(String nestedPath) {
		this.bindingResult.setNestedPath(nestedPath);
	}

	public String getNestedPath() {
		return this.bindingResult.getNestedPath();
	}
	
	@Override
	public String getMessage() {
		return this.bindingResult.toString();
	}

	@Override
	public boolean equals(Object other) {
		return (this == other || this.bindingResult.equals(other));
	}

	@Override
	public int hashCode() {
		return this.bindingResult.hashCode();
	}
}
