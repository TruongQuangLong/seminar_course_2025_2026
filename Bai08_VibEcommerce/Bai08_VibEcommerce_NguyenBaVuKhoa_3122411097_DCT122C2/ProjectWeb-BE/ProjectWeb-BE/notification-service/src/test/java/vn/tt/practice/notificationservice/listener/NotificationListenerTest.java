package vn.tt.practice.notificationservice.listener;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.tt.practice.notificationservice.service.NotificationService;

@ExtendWith(MockitoExtension.class)
class NotificationListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationListener notificationListener;

    @Test
    void listenDelegatesRawMessageToNotificationService() {
        String payload = "{\"orderNumber\":\"ORD123\",\"message\":\"Order Placed Successfully\"}";

        notificationListener.listen(payload);

        verify(notificationService).processRawMessage(payload);
    }
}