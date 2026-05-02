package com.hdbank.orderservice.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineItemsTest {

    @Test
    void testOrderLineItemsBuilder() {
        OrderLineItems item = OrderLineItems.builder()
                .id(1L)
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        assertEquals(1L, item.getId());
        assertEquals("iphone_13", item.getSkuCode());
        assertEquals(BigDecimal.valueOf(1200), item.getPrice());
        assertEquals(1, item.getQuantity());
    }

    @Test
    void testOrderLineItemsNoArgsConstructor() {
        OrderLineItems item = new OrderLineItems();
        assertNull(item.getId());
        assertNull(item.getSkuCode());
        assertNull(item.getPrice());
        assertNull(item.getQuantity());
    }

    @Test
    void testOrderLineItemsAllArgsConstructor() {
        OrderLineItems item = new OrderLineItems(1L, "galaxy_s21", BigDecimal.valueOf(999), 2);

        assertEquals(1L, item.getId());
        assertEquals("galaxy_s21", item.getSkuCode());
        assertEquals(BigDecimal.valueOf(999), item.getPrice());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void testOrderLineItemsSettersAndGetters() {
        OrderLineItems item = new OrderLineItems();
        item.setId(3L);
        item.setSkuCode("pixel_7");
        item.setPrice(BigDecimal.valueOf(599));
        item.setQuantity(3);

        assertEquals(3L, item.getId());
        assertEquals("pixel_7", item.getSkuCode());
        assertEquals(BigDecimal.valueOf(599), item.getPrice());
        assertEquals(3, item.getQuantity());
    }

    @Test
    void testOrderLineItemsEqualsAndHashCode() {
        OrderLineItems item1 = OrderLineItems.builder().id(1L).skuCode("abc").price(BigDecimal.TEN).quantity(1).build();
        OrderLineItems item2 = OrderLineItems.builder().id(1L).skuCode("abc").price(BigDecimal.TEN).quantity(1).build();

        assertEquals(item1, item2);
        assertEquals(item1.hashCode(), item2.hashCode());
    }

    @Test
    void testOrderLineItemsToString() {
        OrderLineItems item = OrderLineItems.builder().id(1L).skuCode("iphone_13").price(BigDecimal.valueOf(1200)).quantity(1).build();
        String toString = item.toString();

        assertTrue(toString.contains("iphone_13"));
        assertTrue(toString.contains("1200"));
    }
}
