package vn.tt.practice.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.tt.practice.notificationservice.dto.NotificationMessage;
import vn.tt.practice.notificationservice.dto.NotificationResult;
import vn.tt.practice.notificationservice.service.NotificationService;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResult> processNotification(@Valid @RequestBody NotificationMessage notificationMessage) {
        NotificationResult result = notificationService.process(notificationMessage);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(result);
    }
}