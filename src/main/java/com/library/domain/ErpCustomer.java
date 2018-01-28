package com.library.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "ERPCustomer")
public class ErpCustomer {

    @JsonProperty("CustomerId")
    @XmlElement(name = "CustomerId")
    private String customerId;
    @JsonProperty("FullName")
    @XmlElement(name = "FullName")
    private String fullName;
    @JsonProperty("Age")
    @XmlElement(name = "Age")
    private String age;
    @JsonProperty("PhoneNumber")
    @XmlElement(name = "PhoneNumber")
    private String phoneNumber;
    @JsonProperty("Address")
    @XmlElement(name = "Address")
    private String address;
    @JsonProperty("EmailAddress")
    @XmlElement(name = "EmailAddress")
    private String email;
    @JsonProperty("Country")
    @XmlElement(name = "Country")
    private String country;

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
