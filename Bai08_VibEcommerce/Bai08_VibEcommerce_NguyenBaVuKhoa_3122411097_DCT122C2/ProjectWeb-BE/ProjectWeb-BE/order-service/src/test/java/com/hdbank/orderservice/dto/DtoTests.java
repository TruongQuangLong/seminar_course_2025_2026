package com.hdbank.orderservice.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoTests {

    // ===== OrderLineItemsDto Tests =====

    @Test
    void testOrderLineItemsDtoBuilder() {
        OrderLineItemsDto dto = OrderLineItemsDto.builder()
                .id(1L)
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("iphone_13", dto.getSkuCode());
        assertEquals(BigDecimal.valueOf(1200), dto.getPrice());
        assertEquals(1, dto.getQuantity());
    }

    @Test
    void testOrderLineItemsDtoNoArgsConstructor() {
        OrderLineItemsDto dto = new OrderLineItemsDto();
        assertNull(dto.getId());
        assertNull(dto.getSkuCode());
        assertNull(dto.getPrice());
        assertNull(dto.getQuantity());
    }

    @Test
    void testOrderLineItemsDtoSetters() {
        OrderLineItemsDto dto = new OrderLineItemsDto();
        dto.setId(5L);
        dto.setSkuCode("macbook_pro");
        dto.setPrice(BigDecimal.valueOf(2499));
        dto.setQuantity(2);

        assertEquals(5L, dto.getId());
        assertEquals("macbook_pro", dto.getSkuCode());
        assertEquals(BigDecimal.valueOf(2499), dto.getPrice());
        assertEquals(2, dto.getQuantity());
    }

    // ===== OrderRequest Tests =====

    @Test
    void testOrderRequestBuilder() {
        OrderLineItemsDto item = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        OrderRequest request = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(item))
                .build();

        assertNotNull(request.getOrderLineItemsDtoList());
        assertEquals(1, request.getOrderLineItemsDtoList().size());
        assertEquals("iphone_13", request.getOrderLineItemsDtoList().get(0).getSkuCode());
    }

    @Test
    void testOrderRequestNoArgsConstructor() {
        OrderRequest request = new OrderRequest();
        assertNull(request.getOrderLineItemsDtoList());
    }

    @Test
    void testOrderRequestAllArgsConstructor() {
        OrderLineItemsDto item = new OrderLineItemsDto(1L, "pixel_7", BigDecimal.valueOf(599), 1);
        OrderRequest request = new OrderRequest(List.of(item));

        assertEquals(1, request.getOrderLineItemsDtoList().size());
    }

    // ===== InventoryResponse Tests =====

    @Test
    void testInventoryResponseBuilder() {
        InventoryResponse response = InventoryResponse.builder()
                .skuCode("iphone_13")
                .isInStock(true)
                .build();

        assertEquals("iphone_13", response.getSkuCode());
        assertTrue(response.isInStock());
    }

    @Test
    void testInventoryResponseNotInStock() {
        InventoryResponse response = InventoryResponse.builder()
                .skuCode("out_of_stock_item")
                .isInStock(false)
                .build();

        assertEquals("out_of_stock_item", response.getSkuCode());
        assertFalse(response.isInStock());
    }

    @Test
    void testInventoryResponseNoArgsConstructor() {
        InventoryResponse response = new InventoryResponse();
        assertNull(response.getSkuCode());
        assertFalse(response.isInStock());
    }

    @Test
    void testInventoryResponseSetters() {
        InventoryResponse response = new InventoryResponse();
        response.setSkuCode("galaxy_s21");
        response.setInStock(true);

        assertEquals("galaxy_s21", response.getSkuCode());
        assertTrue(response.isInStock());
    }

    // ===== OrderEvent Tests =====

    @Test
    void testOrderEventBuilder() {
        OrderEvent event = OrderEvent.builder()
                .orderNumber("ORD-001")
                .message("Order Placed Successfully")
                .build();

        assertEquals("ORD-001", event.getOrderNumber());
        assertEquals("Order Placed Successfully", event.getMessage());
    }

    @Test
    void testOrderEventNoArgsConstructor() {
        OrderEvent event = new OrderEvent();
        assertNull(event.getOrderNumber());
        assertNull(event.getMessage());
    }

    @Test
    void testOrderEventAllArgsConstructor() {
        OrderEvent event = new OrderEvent("ORD-002", "Order Cancelled");

        assertEquals("ORD-002", event.getOrderNumber());
        assertEquals("Order Cancelled", event.getMessage());
    }

    @Test
    void testOrderEventSetters() {
        OrderEvent event = new OrderEvent();
        event.setOrderNumber("ORD-003");
        event.setMessage("Order Updated");

        assertEquals("ORD-003", event.getOrderNumber());
        assertEquals("Order Updated", event.getMessage());
    }

    @Test
    void testOrderEventEqualsAndHashCode() {
        OrderEvent event1 = new OrderEvent("ORD-001", "msg");
        OrderEvent event2 = new OrderEvent("ORD-001", "msg");

        assertEquals(event1, event2);
        assertEquals(event1.hashCode(), event2.hashCode());
    }
}
