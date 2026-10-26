package com.orders.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class OrderEventPublisher {

    private static final String TOPIC = "order-created";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            CompletableFuture<?> future = kafkaTemplate.send(TOPIC, event.orderId().toString(), event);
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish OrderCreatedEvent for orderId={}: {}",
                            event.orderId(), ex.getMessage(), ex);
                } else {
                    log.info("Published OrderCreatedEvent for orderId={} to topic '{}'",
                            event.orderId(), TOPIC);
                }
            });
        } catch (Exception e) {
            log.error("Error sending OrderCreatedEvent for orderId={}: {}", event.orderId(), e.getMessage(), e);
        }
    }
}
