package com.library.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component("jmsMessageSender")
public class JMSMessageSender {

    @Qualifier("notificationJmsTemplate")
    @Autowired
    protected JmsTemplate jmsTemplate;
    protected Queue replyToDestination;

    private static Logger logger = LoggerFactory.getLogger(JMSMessageSender.class);

    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public Queue getReplyToDestination() {
        return replyToDestination;
    }

    public void setReplyToDestination(Queue replyToDestination) {
        this.replyToDestination = replyToDestination;
    }

    public void sendMessage(final String payload) throws JMSException {
        jmsTemplate.send(new MessageCreator() {
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage(payload);
                message.setJMSReplyTo(replyToDestination);
                logger.info(String.format("JMSMessageSender.sendMessage '{%s}'", payload));
                return message;
            }
        });
    }
}
