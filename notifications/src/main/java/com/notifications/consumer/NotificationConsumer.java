package com.notifications.consumer;

import com.notifications.dto.NotificationEvent;
import com.notifications.dto.OrderEvent;
import com.notifications.model.Notification;
import com.notifications.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import tools.jackson.databind.ObjectMapper;

/**
 * Consumes order events from Kafka (topic: order-created) and turns each one
 * into a persisted notification.
 *
 * <p>The raw JSON payload is received as a {@code String} and parsed with the
 * Spring-managed Jackson {@link ObjectMapper}, so the Kafka deserializer is not
 * coupled to a specific Jackson version.
 */
@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationConsumer(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.order-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void onOrderEvent(
            @Payload String payload,
            @Header(name = KafkaHeaders.RECEIVED_TOPIC, required = false) String topic,
            @Header(name = KafkaHeaders.OFFSET, required = false) Long offset) {

        log.info("Received message from topic '{}' (offset {}): {}", topic, offset, payload);

        if (!StringUtils.hasText(payload) || !payload.trim().startsWith("{")) {
            log.warn("Skipping non-JSON message: {}", payload);
            return;
        }

        OrderEvent order;
        try {
            order = objectMapper.readValue(payload, OrderEvent.class);
        } catch (Exception e) {
            log.warn("Failed to parse order event, discarding message: {}", payload, e);
            return;
        }

        if (order == null || order.getOrderId() == null) {
            log.warn("Discarding order event without an orderId: {}", payload);
            return;
        }

        NotificationEvent event = toNotification(order);
        Notification saved = notificationService.createFromEvent(event);
        log.info("Persisted notification id={} for recipient '{}' (orderId={})",
                saved.getId(), saved.getRecipient(), order.getOrderId());
    }

    /**
     * Maps an order event to a notification.
     * recipient = the user the order belongs to; message = a human-readable summary.
     */
    private NotificationEvent toNotification(OrderEvent order) {
        String recipient = order.getUserId() != null ? "user-" + order.getUserId() : "unknown";

        StringBuilder message = new StringBuilder();
        if (order.getEventType() != null) {
            message.append(order.getEventType()).append(": ");
        }
        message.append("Order #").append(order.getOrderId());
        if (StringUtils.hasText(order.getProduct())) {
            message.append(" (").append(order.getProduct()).append(")");
        }

        return new NotificationEvent(recipient, message.toString());
    }
}
