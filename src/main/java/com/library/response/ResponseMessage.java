package com.library.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseMessage {

    @XmlElement
    private String messageId;
    @XmlElement
    private MessageSeverity severity;
    @XmlElement
    private MessageCode messageCode;
    @XmlElement
    private String message;

    public ResponseMessage() {
        super();
        messageId = "01";
        severity = MessageSeverity.ERROR;
        messageCode = MessageCode.UNKNOWN_ERROR;
        message = MessageCode.UNKNOWN_ERROR.name();
    }

    public MessageSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(MessageSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public MessageCode getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(MessageCode messageCode) {
        this.messageCode = messageCode;
    }
}
