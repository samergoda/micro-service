package com.orders.event;

public record OrderCreatedEvent(
        Long orderId,
        Long userId,
        String eventType,
        String product
) {
}