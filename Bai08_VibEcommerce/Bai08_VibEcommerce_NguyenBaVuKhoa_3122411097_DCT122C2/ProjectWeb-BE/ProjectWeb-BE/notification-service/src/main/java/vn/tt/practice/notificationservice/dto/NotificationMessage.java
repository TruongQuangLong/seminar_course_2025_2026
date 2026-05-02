package vn.tt.practice.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationMessage(
        @NotBlank String orderNumber,
        @NotBlank String message) {
}