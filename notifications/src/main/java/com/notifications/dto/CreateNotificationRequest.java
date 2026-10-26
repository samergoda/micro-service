package com.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateNotificationRequest {

    @NotBlank(message = "recipient must not be blank")
    private String recipient;

    @NotBlank(message = "message must not be blank")
    @Size(max = 1000, message = "message must be at most 1000 characters")
    private String message;

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
}
