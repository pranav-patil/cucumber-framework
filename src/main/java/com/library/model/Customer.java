package com.library.model;

public class Customer {

	private String firstName;
	private String lastName;
	private String age;
	private String phoneNumber;
	private String address;
	private String email;
	private String country;

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
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

	public Customer() {
	}

	@Override
	public String toString() {
		return "Customer [firstName=" + firstName
				+ ", lastName=" + lastName
				+ ", age=" + age
				+ ", phoneNumber=" + phoneNumber
				+ ", address=" + address
				+ ", email=" + email
				+ ", country=" + country
				+ "]";
	}
}