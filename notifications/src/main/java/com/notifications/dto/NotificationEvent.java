package com.notifications.dto;

/**
 * Represents a notification event received from Kafka.
 * Deserialized from the JSON payload of incoming messages.
 */
public class NotificationEvent {

    private String recipient;
    private String message;

    public NotificationEvent() {
        // Required for JSON deserialization
    }

    public NotificationEvent(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "NotificationEvent{recipient='" + recipient + "', message='" + message + "'}";
    }
}
