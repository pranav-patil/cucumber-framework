package com.library.controller;

import com.library.dao.ERPServiceAdapter;
import com.library.domain.ErpCustomer;
import com.library.domain.ErpResponse;
import com.library.model.Customer;
import com.library.mongodb.dao.CustomerDAO;
import com.library.mongodb.dao.SequenceDAO;
import com.library.response.*;
import com.library.validation.Validate;
import org.dozer.DozerBeanMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	@Autowired
	private ERPServiceAdapter erpServiceAdapter;
	@Autowired
	private CustomerDAO customerDAO;
	@Autowired
	private SequenceDAO sequenceDAO;
	@Autowired
	private DozerBeanMapper dozerBeanMapper ;

	Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@PostMapping(value = "/add", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE},
								 produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Validate(type=Customer.class, validators={"customerValidator"})
	public ServiceResponse addCustomer(@RequestBody Customer customer, HttpServletRequest request) throws Exception {
		ServiceResponse serviceResponse = new ServiceResponse();

		String fullName = customer.getFirstName() + " " + customer.getLastName();
		com.library.mongodb.domain.Customer customerDomain = dozerBeanMapper.map(customer, com.library.mongodb.domain.Customer.class);
		Long customerId = sequenceDAO.getNextSequence("CustomerSeq", 1L);
		customerDomain.setCustomerId(customerId.toString());
		customerDomain.setName(fullName);
		customerDomain.setLocked(false);
		customerDomain.setCreationDate(new Date());
		customerDomain.setLastUpdatedDate(new Date());
		customerDAO.save(customerDomain);

		ErpCustomer erpCustomer = new ErpCustomer();
		erpCustomer.setCustomerId(customerId.toString());
		erpCustomer.setFullName(fullName);
		erpCustomer.setCountry(customer.getCountry());

		String accept = request.getHeader(HttpHeaders.ACCEPT);
		MediaType mediaType = MediaType.APPLICATION_JSON;

		if(MediaType.APPLICATION_XML.toString().equals(accept)) {
			mediaType = MediaType.APPLICATION_XML;
		}

		String response = erpServiceAdapter.post("/internal/erp/addCustomer", erpCustomer, mediaType);
		ErpResponse erpResponse = erpServiceAdapter.getObject(response, ErpResponse.class);

		if("Success".equals(erpResponse.getStatus())) {
			serviceResponse.addMessage(getResponseMessage(MessageCode.CUSTOMER_ADDED, MessageSeverity.SUCCESS));
		} else {
			serviceResponse.addMessage(getResponseMessage(MessageCode.CUSTOMER_CREATION_FAILED, MessageSeverity.ERROR));
			logger.error(erpResponse.getMessage());
		}

		return serviceResponse;
	}

	@GetMapping(value = "/id/{customerId}", produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
	@PreAuthorize("hasAnyRole('ADMIN')")
	public CustomerResponse getCustomer(@PathVariable("customerId") String customerId) {
		com.library.mongodb.domain.Customer customerDomain = customerDAO.findById(Long.valueOf(customerId));
		Customer customer = dozerBeanMapper.map(customerDomain, Customer.class);
		CustomerResponse customerResponse = new CustomerResponse();
		customerResponse.setCustomer(customer);
		customerResponse.setMessages(Collections.singletonList(getResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS)));
		return customerResponse;
	}

	@GetMapping(value = "/all", produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
	@PreAuthorize("hasAnyRole('ADMIN')")
	public CustomerListResponse getAllCustomers(HttpServletRequest request) throws IOException {

		String accept = request.getHeader(HttpHeaders.ACCEPT);
		MediaType mediaType = MediaType.APPLICATION_JSON;

		if(MediaType.APPLICATION_XML.toString().equals(accept)) {
			mediaType = MediaType.APPLICATION_XML;
		}

		String response = erpServiceAdapter.getRequest("/internal/erp/allCustomers", mediaType);
		List<ErpCustomer> erpCustomers = erpServiceAdapter.getObjectList(response, ErpCustomer.class);

		List<Customer> customers = erpCustomers.stream()
												.map(p -> createCustomer(p))
												.collect(Collectors.toList());

		CustomerListResponse customerListResponse = new CustomerListResponse();
		customerListResponse.setCustomers(customers);
		customerListResponse.setMessages(Collections.singletonList(getResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS)));
		return customerListResponse;
	}

	private Customer createCustomer(ErpCustomer p) {
		Customer customer = dozerBeanMapper.map(p, Customer.class);
		String[] names = p.getFullName().split(" ");
		if(names.length == 2) {
            customer.setLastName(names[1]);
        }
		if(names.length > 0) {
            customer.setFirstName(names[0]);
        }
		return customer;
	}

	private ResponseMessage getResponseMessage(MessageCode messageCode, MessageSeverity messageSeverity) {
		ResponseMessage responseMessage = new ResponseMessage();
		responseMessage.setMessageCode(messageCode);
		responseMessage.setMessage(messageCode.value());
		responseMessage.setSeverity(messageSeverity);
		return responseMessage;
	}
}