package com.emprovise.response;

public class ResponseMessage {

    private String messageId;
    private MessageSeverity severity;
    private MessageCode messageCode;
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
