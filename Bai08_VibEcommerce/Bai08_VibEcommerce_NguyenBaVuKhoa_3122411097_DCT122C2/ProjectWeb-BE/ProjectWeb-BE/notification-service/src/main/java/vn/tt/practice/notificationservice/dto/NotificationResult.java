package vn.tt.practice.notificationservice.dto;

import java.time.Instant;

public record NotificationResult(
        String orderNumber,
        String message,
        String status,
        Instant processedAt) {
}