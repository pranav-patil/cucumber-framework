package com.library.controller;

import com.library.hibernate.dao.AccountDAO;
import com.library.hibernate.domain.Account;
import com.library.model.Transaction;
import com.library.mongodb.dao.CustomerDAO;
import com.library.mongodb.domain.Customer;
import com.library.response.MessageCode;
import com.library.response.MessageSeverity;
import com.library.response.ResponseMessage;
import com.library.response.ServiceResponse;
import com.library.validation.ServiceException;
import com.library.validation.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private CustomerDAO customerDAO;
    @Autowired
    private AccountDAO accountDAO;

    @PostMapping(value = "/setup", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE},
            produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Validate(type = com.library.model.Account.class, validators = {"accountValidator"})
    public ServiceResponse setupAccount(@RequestBody com.library.model.Account account) {
        ServiceResponse serviceResponse = new ServiceResponse();

        Account accountDomain = new Account();
        accountDomain.setCustomerId(account.getCustomerId());
        accountDomain.setPasscode(account.getPasscode());
        accountDomain.setBranchId(account.getBranchId());
        accountDomain.setBalance(account.getBalance());
        accountDomain.setModifiedDate(new Date());

        Customer customer = customerDAO.findById(Long.valueOf(account.getCustomerId()));
        if(customer != null) {
            accountDomain.setFirstName(customer.getFirstName());
            accountDomain.setLastName(customer.getLastName());
            accountDomain.setEmail(customer.getEmail());
        }

        accountDAO.save(accountDomain);
        serviceResponse.setMessages(Collections.singletonList(new ResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS)));
        return serviceResponse;
    }

    @PostMapping(value = "/transferFunds", consumes = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE},
            produces = {APPLICATION_JSON_VALUE, APPLICATION_XML_VALUE})
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Validate(type = Transaction.class, validators = {"transactionValidator"})
    public ServiceResponse transferFunds(@RequestBody Transaction transaction) {
        ServiceResponse serviceResponse = new ServiceResponse();

        Optional<Account> senderAccountOptional = accountDAO.findByAccountId(transaction.getAccountNumber());
        if(!senderAccountOptional.isPresent()) {
            throw new ServiceException(MessageCode.VALIDATION_FAILED, String.format("Sender Account Id %s not present.", transaction.getAccountNumber()));
        }

        Optional<Account> receiverAccountOptional = accountDAO.findByAccountId(transaction.getRecipientAccount());
        if(!receiverAccountOptional.isPresent()) {
            throw new ServiceException(MessageCode.VALIDATION_FAILED, String.format("Receiver Account Id %s not present.", transaction.getRecipientAccount()));
        }

        Account senderAccount = senderAccountOptional.get();
        senderAccount.setBalance(senderAccount.getBalance().subtract(transaction.getAmount()));

        Account recipientAccount = receiverAccountOptional.get();
        recipientAccount.setBalance(recipientAccount.getBalance().add(transaction.getAmount()));

        accountDAO.save(senderAccount);
        accountDAO.save(recipientAccount);

        serviceResponse.setMessages(Collections.singletonList(new ResponseMessage(MessageCode.SUCCESS, MessageSeverity.SUCCESS)));
        return serviceResponse;
    }
}