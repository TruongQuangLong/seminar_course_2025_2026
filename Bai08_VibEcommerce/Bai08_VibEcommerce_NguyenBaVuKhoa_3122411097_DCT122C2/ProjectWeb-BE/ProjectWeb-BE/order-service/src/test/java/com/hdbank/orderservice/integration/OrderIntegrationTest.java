package com.hdbank.orderservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hdbank.orderservice.dto.InventoryResponse;
import com.hdbank.orderservice.dto.OrderLineItemsDto;
import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WebClient.Builder webClientBuilder;

    @SuppressWarnings("rawtypes")
    @MockitoBean
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @MockitoBean
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @MockitoBean
    private WebClient.ResponseSpec responseSpec;

    @MockitoBean
    private WebClient webClient;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        // Wire up the mock WebClient chain
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    @DisplayName("Integration Test - Place order successfully when product is in stock")
    void placeOrder_WhenProductInStock_ShouldSaveOrderAndReturn201() throws Exception {
        // Arrange
        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(true).build()
        };
        when(responseSpec.bodyToMono(InventoryResponse[].class))
                .thenReturn(Mono.just(inventoryResponses));

        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("iphone_13")
                .price(BigDecimal.valueOf(1200))
                .quantity(1)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Placed"));

        // Verify order was persisted in DB
        assertEquals(1, orderRepository.findAll().size());
    }

    @Test
    @DisplayName("Integration Test - Place order fails when product is NOT in stock")
    void placeOrder_WhenProductNotInStock_ShouldNotSaveOrderAndReturnError() throws Exception {
        // Arrange
        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("out_of_stock_item").isInStock(false).build()
        };
        when(responseSpec.bodyToMono(InventoryResponse[].class))
                .thenReturn(Mono.just(inventoryResponses));

        OrderLineItemsDto itemDto = OrderLineItemsDto.builder()
                .skuCode("out_of_stock_item")
                .price(BigDecimal.valueOf(500))
                .quantity(1)
                .build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(itemDto))
                .build();

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product is not in stock, please try again later"));

        // Verify order was NOT persisted
        assertEquals(0, orderRepository.findAll().size());
    }

    @Test
    @DisplayName("Integration Test - Place order with multiple items, all in stock")
    void placeOrder_WithMultipleItems_AllInStock_ShouldSucceed() throws Exception {
        // Arrange
        InventoryResponse[] inventoryResponses = {
                InventoryResponse.builder().skuCode("iphone_13").isInStock(true).build(),
                InventoryResponse.builder().skuCode("galaxy_s21").isInStock(true).build()
        };
        when(responseSpec.bodyToMono(InventoryResponse[].class))
                .thenReturn(Mono.just(inventoryResponses));

        OrderLineItemsDto item1 = OrderLineItemsDto.builder()
                .skuCode("iphone_13").price(BigDecimal.valueOf(1200)).quantity(1).build();
        OrderLineItemsDto item2 = OrderLineItemsDto.builder()
                .skuCode("galaxy_s21").price(BigDecimal.valueOf(999)).quantity(2).build();
        OrderRequest orderRequest = OrderRequest.builder()
                .orderLineItemsDtoList(List.of(item1, item2))
                .build();

        // Act & Assert
        var mvcResult = mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Placed"));

        // Verify persisted
        var orders = orderRepository.findAll();
        assertEquals(1, orders.size());
        assertEquals(2, orders.get(0).getOrderLineItemsList().size());
    }
}
