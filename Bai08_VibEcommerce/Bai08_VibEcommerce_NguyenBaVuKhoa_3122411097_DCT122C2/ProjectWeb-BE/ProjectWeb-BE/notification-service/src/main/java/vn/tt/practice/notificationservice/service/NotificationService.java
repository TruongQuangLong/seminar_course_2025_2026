package vn.tt.practice.notificationservice.service;

import java.time.Instant;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tt.practice.notificationservice.dto.NotificationMessage;
import vn.tt.practice.notificationservice.dto.NotificationResult;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;

    public NotificationResult processRawMessage(String rawMessage) {
        try {
            NotificationMessage notificationMessage = objectMapper.readValue(rawMessage, NotificationMessage.class);
            return process(notificationMessage);
        } catch (Exception exception) {
            throw new NotificationProcessingException("Failed to parse notification message", exception);
        }
    }

    public NotificationResult process(NotificationMessage notificationMessage) {
        if (notificationMessage == null) {
            throw new NotificationProcessingException("Notification message must not be null");
        }

        log.info("Received notification for orderNumber={}, message={}",
                notificationMessage.orderNumber(),
                notificationMessage.message());

        emailService.simulateEmail(notificationMessage);

        return new NotificationResult(
                notificationMessage.orderNumber(),
                notificationMessage.message(),
                "PROCESSED",
                Instant.now());
    }
}