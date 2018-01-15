package com.library.jms;

import com.library.domain.NotificationRequest;
import com.library.mongodb.dao.NotificationDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component("jmsMessageListener")
public class JMSMessageListener implements MessageListener {

    @Autowired
    @Qualifier("oxmMessageConverter")
    private MessageConverter messageConverter;
    @Autowired
    private NotificationDAO notificationDAO;

    private static Logger logger = LoggerFactory.getLogger(JMSMessageListener.class);

    @Override
    public void onMessage(Message message) {

        try {
            Object object = messageConverter.fromMessage(message);

            if(object instanceof NotificationRequest) {
                processMessage((NotificationRequest) object);
            } else {
                logger.error("Unknown message: " + getTextMessage(message));
            }

        } catch (Exception ex) {

            String text = getTextMessage(message);

            if (message != null) {
                logger.error("Error processing notification message: " + text, ex);
            } else {
                logger.error("Error processing notification message.", ex);
            }
        }
    }

    private String getTextMessage(Message message) {
        String messageText = null;
        try {
            if (message instanceof TextMessage) {
                messageText = ((TextMessage)message).getText();
            }
        } catch (JMSException e) { }
        return messageText;
    }

    private void processMessage(NotificationRequest notificationRequest) {

    }
}
