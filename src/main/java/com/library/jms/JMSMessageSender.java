package com.library.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

@Component("jmsMessageSender")
public class JMSMessageSender {

    @Qualifier("notificationJmsTemplate")
    @Autowired
    private JmsTemplate jmsTemplate;
    private Queue replyDestination;

    private static Logger logger = LoggerFactory.getLogger(JMSMessageSender.class);

    public Queue getReplyDestination() {
        return replyDestination;
    }

    public void setReplyDestination(Queue replyDestination) {
        this.replyDestination = replyDestination;
    }

    public void sendMessage(final String payload) throws JMSException {
        jmsTemplate.send((Session session) ->  {
            TextMessage message = session.createTextMessage(payload);
            message.setJMSReplyTo(replyDestination);
            logger.info(String.format("JMSMessageSender.sendMessage '{%s}'", payload));
            return message;
        });
    }
}
