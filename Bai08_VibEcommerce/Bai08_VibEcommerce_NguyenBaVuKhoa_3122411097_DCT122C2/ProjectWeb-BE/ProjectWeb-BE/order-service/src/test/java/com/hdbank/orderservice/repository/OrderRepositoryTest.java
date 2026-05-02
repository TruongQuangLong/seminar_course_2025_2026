package com.hdbank.orderservice.repository;

import com.hdbank.orderservice.model.Order;
import com.hdbank.orderservice.model.OrderLineItems;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    void saveOrder_ShouldPersistOrderWithLineItems() {
        // Arrange
        OrderLineItems item1 = OrderLineItems.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();

        OrderLineItems item2 = OrderLineItems.builder()
                .skuCode("galaxy_s21")
                .price(BigDecimal.valueOf(999))
                .quantity(2)
                .build();

        Order order = Order.builder()
                .orderNumber("ORD-TEST-001")
                .orderLineItemsList(List.of(item1, item2))
                .build();

        // Act
        Order savedOrder = orderRepository.save(order);

        // Assert
        assertNotNull(savedOrder.getId());
        assertEquals("ORD-TEST-001", savedOrder.getOrderNumber());
        assertEquals(2, savedOrder.getOrderLineItemsList().size());
    }

    @Test
    void findById_ShouldReturnOrder() {
        // Arrange
        OrderLineItems item = OrderLineItems.builder()
                .skuCode("macbook_pro")
                .price(BigDecimal.valueOf(2499))
                .quantity(1)
                .build();

        Order order = Order.builder()
                .orderNumber("ORD-TEST-002")
                .orderLineItemsList(List.of(item))
                .build();

        Order savedOrder = orderRepository.save(order);

        // Act
        Optional<Order> foundOrder = orderRepository.findById(savedOrder.getId());

        // Assert
        assertTrue(foundOrder.isPresent());
        assertEquals("ORD-TEST-002", foundOrder.get().getOrderNumber());
        assertEquals(1, foundOrder.get().getOrderLineItemsList().size());
        assertEquals("macbook_pro", foundOrder.get().getOrderLineItemsList().get(0).getSkuCode());
    }

    @Test
    void findAll_ShouldReturnAllOrders() {
        // Arrange
        Order order1 = Order.builder()
                .orderNumber("ORD-001")
                .orderLineItemsList(List.of(
                        OrderLineItems.builder().skuCode("item_a").price(BigDecimal.TEN).quantity(1).build()
                ))
                .build();

        Order order2 = Order.builder()
                .orderNumber("ORD-002")
                .orderLineItemsList(List.of(
                        OrderLineItems.builder().skuCode("item_b").price(BigDecimal.valueOf(20)).quantity(2).build()
                ))
                .build();

        orderRepository.save(order1);
        orderRepository.save(order2);

        // Act
        List<Order> orders = orderRepository.findAll();

        // Assert
        assertEquals(2, orders.size());
    }

    @Test
    void deleteById_ShouldRemoveOrder() {
        // Arrange
        Order order = Order.builder()
                .orderNumber("ORD-DEL-001")
                .orderLineItemsList(List.of(
                        OrderLineItems.builder().skuCode("to_delete").price(BigDecimal.ONE).quantity(1).build()
                ))
                .build();

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getId();

        // Act
        orderRepository.deleteById(orderId);

        // Assert
        Optional<Order> deleted = orderRepository.findById(orderId);
        assertFalse(deleted.isPresent());
    }

    @Test
    void cascadePersist_ShouldPersistLineItemsWithOrder() {
        // Arrange
        OrderLineItems item = OrderLineItems.builder()
                .skuCode("cascade_test")
                .price(BigDecimal.valueOf(100))
                .quantity(5)
                .build();

        Order order = Order.builder()
                .orderNumber("ORD-CASCADE")
                .orderLineItemsList(List.of(item))
                .build();

        // Act
        Order saved = orderRepository.save(order);

        // Assert - line items should have been persisted via cascade
        assertNotNull(saved.getOrderLineItemsList().get(0).getId());
        assertEquals("cascade_test", saved.getOrderLineItemsList().get(0).getSkuCode());
        assertEquals(BigDecimal.valueOf(100), saved.getOrderLineItemsList().get(0).getPrice());
        assertEquals(5, saved.getOrderLineItemsList().get(0).getQuantity());
    }
}
