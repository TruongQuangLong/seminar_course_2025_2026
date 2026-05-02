package com.hdbank.orderservice.service;

import com.hdbank.orderservice.dto.*;
import com.hdbank.orderservice.model.Order;
import com.hdbank.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();
    }

    @Test
    void placeOrder_WhenProductIsInStock_ShouldSaveOrderAndSendKafkaEvent() throws Exception {
        // Arrange
        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(true).build()
        };

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setId(1L);
            return order;
        });

        // Act
        CompletableFuture<String> result = orderService.placeOrder(orderRequest);
        String response = result.get();

        // Assert
        assertEquals("Order Placed", response);

        // Verify order was saved
        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertNotNull(savedOrder.getOrderNumber());
        assertEquals(1, savedOrder.getOrderLineItemsList().size());
        assertEquals("iphone_13", savedOrder.getOrderLineItemsList().get(0).getSkuCode());

        // Verify Kafka event was sent
        ArgumentCaptor<OrderEvent> eventCaptor = ArgumentCaptor.forClass(OrderEvent.class);
        verify(kafkaTemplate, times(1)).send(eq("notificationTopic"), eventCaptor.capture());
        OrderEvent sentEvent = eventCaptor.getValue();
        assertEquals(savedOrder.getOrderNumber(), sentEvent.getOrderNumber());
        assertEquals("Order Placed Successfully", sentEvent.getMessage());
    }

    @Test
    void placeOrder_WhenProductIsNotInStock_ShouldThrowException() {
        // Arrange
        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(false).build()
        };

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        // Act & Assert
        CompletableFuture<String> result = orderService.placeOrder(orderRequest);

        Exception exception = assertThrows(Exception.class, result::get);
        assertTrue(exception.getCause().getMessage().contains("Product is not in stock"));

        // Verify order was NOT saved
        verify(orderRepository, never()).save(any(Order.class));

        // Verify Kafka event was NOT sent
        verify(kafkaTemplate, never()).send(anyString(), any(OrderEvent.class));
    }

    @Test
    void placeOrder_WhenInventoryReturnsEmptyArray_ShouldThrowException() {
        // Arrange
        InventoryResponse[] inventoryResponses = {};

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        // Act & Assert
        CompletableFuture<String> result = orderService.placeOrder(orderRequest);

        Exception exception = assertThrows(Exception.class, result::get);
        assertTrue(exception.getCause().getMessage().contains("Product is not in stock"));

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_WithMultipleItems_AllInStock_ShouldSucceed() throws Exception {
        // Arrange
        OrderLineItemsDto item1 = OrderLineItemsDto.builder().skuCode("iphone_13").price(BigDecimal.valueOf(1200)).quantity(1).build();
        OrderLineItemsDto item2 = OrderLineItemsDto.builder().skuCode("galaxy_s21").price(BigDecimal.valueOf(999)).quantity(2).build();
        OrderRequest multiRequest = OrderRequest.builder().orderLineItemsDtoList(List.of(item1, item2)).build();

        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(true).build(),
                InventoryResponse.builder().skuCode("galaxy_s21").isInStock(true).build()
        };

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        String response = orderService.placeOrder(multiRequest).get();

        // Assert
        assertEquals("Order Placed", response);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(eq("notificationTopic"), any(OrderEvent.class));
    }

    @Test
    void placeOrder_WithMultipleItems_OneNotInStock_ShouldFail() {
        // Arrange
        OrderLineItemsDto item1 = OrderLineItemsDto.builder().skuCode("iphone_13").price(BigDecimal.valueOf(1200)).quantity(1).build();
        OrderLineItemsDto item2 = OrderLineItemsDto.builder().skuCode("out_of_stock").price(BigDecimal.valueOf(500)).quantity(1).build();
        OrderRequest multiRequest = OrderRequest.builder().orderLineItemsDtoList(List.of(item1, item2)).build();

        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(true).build(),
                InventoryResponse.builder().skuCode("out_of_stock").isInStock(false).build()
        };

        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(InventoryResponse[].class)).thenReturn(Mono.just(inventoryResponses));

        // Act & Assert
        CompletableFuture<String> result = orderService.placeOrder(multiRequest);
        Exception exception = assertThrows(Exception.class, result::get);
        assertTrue(exception.getCause().getMessage().contains("Product is not in stock"));

        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), any(OrderEvent.class));
    }

    @Test
    void fallbackMethod_ShouldReturnFallbackMessage() throws Exception {
        // Act
        CompletableFuture<String> result = orderService.fallbackMethod(orderRequest, new RuntimeException("Service unavailable"));
        String response = result.get();

        // Assert
        assertTrue(response.contains("Oops! Something went wrong"));
        assertTrue(response.contains("order cannot be placed"));
    }
}
