package com.library.domain;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NotificationRequest", propOrder = {
        "notificationId",
        "notificationType",
        "notificationMessage",
        "notificationTimestamp"
})
@XmlRootElement(name = "NotificationRequest")
public class NotificationRequest {

    @XmlElement(name = "NotificationId", required = true)
    private String notificationId;
    @XmlElement(name = "NotificationType", required = true)
    private String notificationType;
    @XmlElement(name = "NotificationMessage", required = true)
    private String notificationMessage;
    @XmlElement(name = "NotificationTimestamp", required = true)
    private String notificationTimestamp;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificationTimestamp() {
        return notificationTimestamp;
    }

    public void setNotificationTimestamp(String notificationTimestamp) {
        this.notificationTimestamp = notificationTimestamp;
    }
}
