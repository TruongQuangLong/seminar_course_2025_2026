package com.hdbank.orderservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Example Kafka listener placeholder.
 * In a real microservice architecture, the notification-service would
 * consume messages from notificationTopic. This listener is provided
 * as a reference and can be removed if notification-service handles consumption.
 */
@Component
@Slf4j
public class OrderEventListener {

    // This listener is intentionally left minimal.
    // The actual notification consumption happens in notification-service.
    // Uncomment below if you want order-service to also listen for events:

    // @KafkaListener(topics = "notificationTopic", groupId = "order-group")
    // public void handleOrderEvent(String message) {
    //     log.info("Received order event: {}", message);
    // }
}
