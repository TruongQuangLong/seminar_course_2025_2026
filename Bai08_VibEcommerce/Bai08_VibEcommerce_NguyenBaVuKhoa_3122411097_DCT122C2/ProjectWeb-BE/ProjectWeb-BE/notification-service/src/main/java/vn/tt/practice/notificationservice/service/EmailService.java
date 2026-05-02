package vn.tt.practice.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.tt.practice.notificationservice.dto.NotificationMessage;

@Service
@Slf4j
public class EmailService {

    public void simulateEmail(NotificationMessage notificationMessage) {
        log.info("Simulating email delivery for orderNumber={}, message={}",
                notificationMessage.orderNumber(),
                notificationMessage.message());
    }
}