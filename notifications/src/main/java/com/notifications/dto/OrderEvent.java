package com.notifications.dto;

/**
 * Represents an order event received from Kafka (topic: order-created).
 * Mirrors the payload published by the order service, e.g.:
 * {"orderId":17,"userId":1,"eventType":"ORDER_CREATED"}
 */
public class OrderEvent {

    private Long orderId;
    private Long userId;
    private String eventType;
    private String product;

    public OrderEvent() {
        // Required for JSON deserialization
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "OrderEvent{orderId=" + orderId + ", userId=" + userId
                + ", eventType='" + eventType + "', product='" + product + "'}";
    }
}
