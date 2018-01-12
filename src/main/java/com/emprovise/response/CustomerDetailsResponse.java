package com.emprovise.response;

import com.emprovise.model.Customer;

public class CustomerDetailsResponse extends ServiceResponse {

    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
