package vn.tt.practice.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import vn.tt.practice.notificationservice.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationListener {

    private final NotificationService notificationService;

    @KafkaListener(topics = "${app.kafka.notification-topic:notificationTopic}", groupId = "${spring.kafka.consumer.group-id:notification-group}")
    public void listen(String rawMessage) {
        log.debug("Kafka message received on notificationTopic");
        notificationService.processRawMessage(rawMessage);
    }
}