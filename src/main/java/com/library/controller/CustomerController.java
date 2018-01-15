package com.library.controller;

import com.library.mongodb.dao.CustomerDAO;
import com.library.dao.ERPServiceAdapter;
import com.library.mongodb.dao.SequenceDAO;
import com.library.domain.CustomerRequest;
import com.library.domain.CustomerResponse;
import com.library.model.Customer;
import com.library.response.*;
import com.library.validation.Validate;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Date;

@Controller
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

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ADMIN')")
	@Validate(type=Customer.class, validators={"customerValidator"})
	public ServiceResponse addCustomer(@RequestBody Customer customer) throws Exception {
		ServiceResponse serviceResponse = new ServiceResponse();

		com.library.mongodb.domain.Customer customerDomain = dozerBeanMapper.map(customer, com.library.mongodb.domain.Customer.class);
		Long customerId = sequenceDAO.getNextSequence("CustomerSeq", 1L);
		customerDomain.setCustomerId(customerId.toString());
		customerDomain.setLocked(false);
		customerDomain.setCreationDate(new Date());
		customerDomain.setLastUpdatedDate(new Date());
		customerDomain.setCountry("USA");
		customerDAO.save(customerDomain);

		CustomerRequest customerRequest = new CustomerRequest();
		customerRequest.setCustomerId(customerId.toString());
		customerRequest.setCustomerName(customer.getName());

		String response = erpServiceAdapter.post(customerRequest, "/internal/erp/addCustomer");
		CustomerResponse customerResponse = erpServiceAdapter.getObject(response, CustomerResponse.class);

		if("S".equals(customerResponse.getStatus())) {
			serviceResponse.addMessage(getResponseMessage(MessageCode.CUSTOMER_ADDED, MessageSeverity.SUCCESS));
		} else {
			serviceResponse.addMessage(getResponseMessage(MessageCode.CUSTOMER_CREATION_FAILED, MessageSeverity.ERROR));
			logger.error(customerResponse.getMessage());
		}

		return serviceResponse;
	}

	@RequestMapping(value = "/get/id/{customerId}", method = RequestMethod.GET)
	@ResponseBody
	@PreAuthorize("hasAnyRole('ADMIN')")
	public CustomerDetailsResponse getCustomer(@PathVariable("customerId") String customerId) {
		com.library.mongodb.domain.Customer customerDomain = customerDAO.findById(Long.valueOf(customerId));
		Customer customer = dozerBeanMapper.map(customerDomain, Customer.class);
		CustomerDetailsResponse customerDetailsResponse = new CustomerDetailsResponse();
		customerDetailsResponse.setCustomer(customer);
		customerDetailsResponse.setMessages(Collections.singletonList(getResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS)));
		return customerDetailsResponse;
	}

	private ResponseMessage getResponseMessage(MessageCode messageCode, MessageSeverity messageSeverity) {
		ResponseMessage responseMessage = new ResponseMessage();
		responseMessage.setMessageCode(messageCode);
		responseMessage.setMessage(messageCode.value());
		responseMessage.setSeverity(messageSeverity);
		return responseMessage;
	}
}