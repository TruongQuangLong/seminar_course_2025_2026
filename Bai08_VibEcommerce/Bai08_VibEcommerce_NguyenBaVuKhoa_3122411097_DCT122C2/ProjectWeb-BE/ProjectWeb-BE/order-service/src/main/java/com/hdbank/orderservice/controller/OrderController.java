package com.hdbank.orderservice.controller;

import com.hdbank.orderservice.dto.OrderRequest;
import com.hdbank.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompletableFuture<ResponseEntity<String>> placeOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.placeOrder(orderRequest)
                .thenApply(result -> {
                    if (result.startsWith("Oops")) {
                        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
                    }
                    return ResponseEntity.status(HttpStatus.CREATED).body(result);
                })
                .exceptionally(ex -> {
                    String message = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
                });
    }
}
