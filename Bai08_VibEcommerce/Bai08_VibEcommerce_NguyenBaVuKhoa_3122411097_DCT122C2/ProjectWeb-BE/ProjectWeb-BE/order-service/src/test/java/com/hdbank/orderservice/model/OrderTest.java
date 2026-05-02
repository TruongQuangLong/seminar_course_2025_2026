package com.hdbank.orderservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testOrderBuilder() {
        OrderLineItems item = OrderLineItems.builder()
                .id(1L)
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        Order order = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .orderLineItemsList(List.of(item))
                .build();

        assertEquals(1L, order.getId());
        assertEquals("ORD-001", order.getOrderNumber());
        assertNotNull(order.getOrderLineItemsList());
        assertEquals(1, order.getOrderLineItemsList().size());
    }

    @Test
    void testOrderNoArgsConstructor() {
        Order order = new Order();
        assertNull(order.getId());
        assertNull(order.getOrderNumber());
        assertNull(order.getOrderLineItemsList());
    }

    @Test
    void testOrderAllArgsConstructor() {
        OrderLineItems item = new OrderLineItems(1L, "iphone_13", BigDecimal.valueOf(1200), 1);
        Order order = new Order(1L, "ORD-001", List.of(item));

        assertEquals(1L, order.getId());
        assertEquals("ORD-001", order.getOrderNumber());
        assertEquals(1, order.getOrderLineItemsList().size());
    }

    @Test
    void testOrderSettersAndGetters() {
        Order order = new Order();
        order.setId(2L);
        order.setOrderNumber("ORD-002");
        order.setOrderLineItemsList(List.of());

        assertEquals(2L, order.getId());
        assertEquals("ORD-002", order.getOrderNumber());
        assertTrue(order.getOrderLineItemsList().isEmpty());
    }

    @Test
    void testOrderEqualsAndHashCode() {
        Order order1 = Order.builder().id(1L).orderNumber("ORD-001").build();
        Order order2 = Order.builder().id(1L).orderNumber("ORD-001").build();

        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
    }

    @Test
    void testOrderToString() {
        Order order = Order.builder().id(1L).orderNumber("ORD-001").build();
        String toString = order.toString();

        assertTrue(toString.contains("ORD-001"));
        assertTrue(toString.contains("1"));
    }
}
