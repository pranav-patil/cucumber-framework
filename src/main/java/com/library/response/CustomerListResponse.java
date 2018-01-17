package com.library.response;

import com.library.model.Customer;

import java.util.List;

public class CustomerListResponse extends ServiceResponse {

    private List<Customer> customers;

    public List<Customer> getCustomers() {
        return customers;
    }

    public void setCustomers(List<Customer> customers) {
        this.customers = customers;
    }
}
