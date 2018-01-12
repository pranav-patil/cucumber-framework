package com.emprovise.validation;

import com.emprovise.response.MessageCode;
import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.UUID;

public class ServiceException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 7272218358982289995L;
    private Integer code = HttpStatus.INTERNAL_SERVER_ERROR.value();
    private String errorID = UUID.randomUUID().toString();
    private MessageCode messageCode = MessageCode.UNKNOWN_ERROR;

    public ServiceException(MessageCode messageCode) {
        super();
        this.messageCode = messageCode;
    }

    public ServiceException(MessageCode messageCode, String message) {
        super(message);
        this.messageCode = messageCode;
    }

    public ServiceException(Integer code) {
        this.code = code;
    }

    public ServiceException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public ServiceException(MessageCode messageCode, Integer code) {
        this.code = code;
        this.messageCode = messageCode;
    }

    public ServiceException(MessageCode messageCode, String message, Integer code) {
        super(message);
        this.code = code;
        this.messageCode = messageCode;
    }

    public ServiceException(MessageCode messageCode, String message, Exception exception) {
        super(message, exception);
        this.messageCode = messageCode;
    }

    public ServiceException(Integer code, Exception exception) {
        super(exception);
        this.code = code;
    }

    public ServiceException(Exception exception) {
        super(exception);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(MessageCode messageCode) {
        this.messageCode = messageCode;
    }
}
