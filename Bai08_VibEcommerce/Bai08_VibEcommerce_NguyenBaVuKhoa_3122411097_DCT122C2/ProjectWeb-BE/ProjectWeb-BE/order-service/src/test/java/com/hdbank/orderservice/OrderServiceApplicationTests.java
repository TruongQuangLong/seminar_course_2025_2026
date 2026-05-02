package com.hdbank.orderservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class OrderServiceApplicationTests {

    @Test
    void contextLoads() {
        // Verifies the Spring application context loads successfully
    }
}
