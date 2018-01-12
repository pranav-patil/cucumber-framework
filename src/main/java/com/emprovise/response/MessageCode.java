package com.emprovise.response;

public enum MessageCode {

    SUCCESS("Request successfully completed."),
    SERVICE_TIMEOUT("service time out"),
    VALIDATION_FAILED("Request is not valid."),
    CUSTOMER_ADDED("Customer is created successfully."),
    CUSTOMER_CREATION_FAILED("Error in adding customer. Please try again."),
    UNKNOWN_ERROR("An error has occurred, please try again.");

    private String value;

    MessageCode(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
