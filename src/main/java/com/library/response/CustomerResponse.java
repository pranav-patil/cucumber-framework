package com.library.response;

import com.library.model.Customer;

public class CustomerResponse extends ServiceResponse {

    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
