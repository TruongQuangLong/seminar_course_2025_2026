package com.hdbank.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdbank.orderservice.dto.OrderLineItemsDto;
import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void placeOrder_Success_ShouldReturn201() throws Exception {
        // Arrange
        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenReturn(CompletableFuture.completedFuture("Order Placed"));

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Placed"));

        verify(orderService, times(1)).placeOrder(any(OrderRequest.class));
    }

    @Test
    void placeOrder_ProductNotInStock_ShouldReturn400() throws Exception {
        // Arrange
        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("out_of_stock")
                .price(BigDecimal.valueOf(500))
                .quantity(1)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();

        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new IllegalArgumentException("Product is not in stock, please try again later"));

        when(orderService.placeOrder(any(OrderRequest.class))).thenReturn(failedFuture);

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product is not in stock, please try again later"));
    }

    @Test
    void placeOrder_ServiceUnavailable_FallbackTriggered_ShouldReturn503() throws Exception {
        // Arrange
        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();

        when(orderService.placeOrder(any(OrderRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(
                        "Oops! Something went wrong, order cannot be placed. Please try again later."));

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string("Oops! Something went wrong, order cannot be placed. Please try again later."));
    }
}
