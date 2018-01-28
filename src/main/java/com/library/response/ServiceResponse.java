package com.library.response;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "serviceResponse")
public class ServiceResponse {

    @XmlElementWrapper(name = "messages")
    @XmlElement(name = "message")
    private List<ResponseMessage> messages = new ArrayList<>();

    public List<ResponseMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ResponseMessage> messages) {
        this.messages = messages;
    }

    public void addMessage(ResponseMessage message) {
        this.messages.add(message);
    }

    public void addMessages(List<ResponseMessage> messages) {
        this.messages.addAll(messages);
    }

    public boolean messagesHasSeverity(MessageSeverity severity) {

        for (ResponseMessage message : messages) {
            if (severity.equals(message.getSeverity())) {
                return true;
            }
        }

        return false;
    }

    public boolean messagesHasSeverityAndMessage(MessageSeverity severity, String messageString) {

        for (ResponseMessage message : messages) {
            if (severity.equals(message.getSeverity()) && messageString.equals(message.getMessage())) {
                return true;
            }
        }

        return false;
    }
}