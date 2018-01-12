package com.emprovise.controller;

import com.emprovise.dao.CustomerMongoDAO;
import com.emprovise.dao.ERPServiceAdapter;
import com.emprovise.dao.SequenceDAO;
import com.emprovise.domain.CustomerRequest;
import com.emprovise.domain.CustomerResponse;
import com.emprovise.model.Customer;
import com.emprovise.response.*;
import com.emprovise.validation.Validate;
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
	private CustomerMongoDAO customerMongoDAO;
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

		com.emprovise.mongodb.Customer customerDomain = dozerBeanMapper.map(customer, com.emprovise.mongodb.Customer.class);
		Long customerId = sequenceDAO.getNextSequence("CustomerSeq", 1L);
		customerDomain.setCustomerId(customerId.toString());
		customerDomain.setLocked(false);
		customerDomain.setCreationDate(new Date());
		customerDomain.setLastUpdatedDate(new Date());
		customerDomain.setCountry("USA");
		customerMongoDAO.save(customerDomain);

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
		com.emprovise.mongodb.Customer customerDomain = customerMongoDAO.findById(Long.valueOf(customerId));
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