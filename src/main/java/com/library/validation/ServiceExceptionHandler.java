package com.library.validation;

import com.library.response.MessageCode;
import com.library.response.MessageSeverity;
import com.library.response.ResponseMessage;
import com.library.response.ServiceResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ControllerAdvice(basePackages = {"com.library.controller"} )
public class ServiceExceptionHandler {

    @ExceptionHandler({Exception.class})
    protected Object handleInvalidRequest(Exception exception) {

        Object response;
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        exception.printStackTrace();

        // Get the HTTP status for all ServiceException.
        if (exception instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) exception;

            if (serviceException.getCode() > 300) {
                httpStatus = HttpStatus.valueOf(serviceException.getCode());
            }
        } else if(exception instanceof AccessDeniedException) {
            httpStatus = HttpStatus.UNAUTHORIZED;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ServiceResponse serviceResponse = getServiceResponse(exception, UUID.randomUUID().toString());
        response = new ResponseEntity<Object>(serviceResponse, headers, httpStatus);
        return response;
    }

    private ServiceResponse getServiceResponse(Exception exception, String errorID) {

        ServiceResponse responseBean = new ServiceResponse();
        ResponseMessage message = new ResponseMessage();
        message.setMessageCode(getMessageCode(exception));
        message.setMessage(message.getMessageCode().value());
        message.setSeverity(MessageSeverity.ERROR);
        message.setMessageId(errorID);

        if (exception instanceof ValidationException) {
            ValidationException validationException = (ValidationException) exception;
            responseBean.setMessages(getErrorMessages(validationException));
        } else {
            responseBean.addMessage(message);
        }

        return responseBean;
    }

    private MessageCode getMessageCode(Exception exception) {

        if (exception instanceof ServiceException) {
            return ((ServiceException) exception).getMessageCode();
        } else if(exception instanceof AccessDeniedException) {
            return MessageCode.ACCESS_DENIED;
        }

        return MessageCode.UNKNOWN_ERROR;
    }

    private List<ResponseMessage> getErrorMessages(ValidationException validationException) {
        List<ResponseMessage> messages = new ArrayList<ResponseMessage>();

        List<ObjectError> allErrors = validationException.getBindingResult().getAllErrors();
        for (ObjectError error : allErrors) {

            ResponseMessage message = new ResponseMessage();

            String errorID = UUID.randomUUID().toString();
            message.setMessageId(errorID);
            message.setSeverity(MessageSeverity.ERROR);

            if (error.getCode() != null) {
                message.setMessageCode(MessageCode.valueOf(error.getCode()));
            } else {
                message.setMessageCode(MessageCode.VALIDATION_FAILED);
            }

            message.setMessage(message.getMessageCode().value());
            messages.add(message);
        }

        return messages;
    }
}
