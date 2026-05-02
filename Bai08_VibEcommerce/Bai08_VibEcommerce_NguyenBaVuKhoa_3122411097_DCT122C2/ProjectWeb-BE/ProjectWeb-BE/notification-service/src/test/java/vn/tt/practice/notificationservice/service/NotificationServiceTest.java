package vn.tt.practice.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.tt.practice.notificationservice.dto.NotificationMessage;
import vn.tt.practice.notificationservice.dto.NotificationResult;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private EmailService emailService;

    @Test
    void processRawMessageParsesAndSimulatesEmail() {
        NotificationService notificationService = new NotificationService(objectMapper, emailService);

        String payload = "{\"orderNumber\":\"ORD123\",\"message\":\"Order Placed Successfully\"}";

        NotificationResult result = notificationService.processRawMessage(payload);

        assertThat(result.orderNumber()).isEqualTo("ORD123");
        assertThat(result.message()).isEqualTo("Order Placed Successfully");
        assertThat(result.status()).isEqualTo("PROCESSED");
        assertThat(result.processedAt()).isNotNull();

        verify(emailService).simulateEmail(new NotificationMessage("ORD123", "Order Placed Successfully"));
    }
}