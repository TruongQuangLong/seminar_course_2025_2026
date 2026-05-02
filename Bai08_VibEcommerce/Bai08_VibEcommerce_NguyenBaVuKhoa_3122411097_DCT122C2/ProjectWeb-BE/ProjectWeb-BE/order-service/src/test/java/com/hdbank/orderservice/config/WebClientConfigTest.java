package com.hdbank.orderservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class WebClientConfigTest {

    @Test
    void webClientBuilder_ShouldCreateNonNullBuilder() {
        WebClientConfig config = new WebClientConfig();
        WebClient.Builder builder = config.webClientBuilder();

        assertNotNull(builder);
    }

    @Test
    void webClientBuilder_ShouldBuildWebClient() {
        WebClientConfig config = new WebClientConfig();
        WebClient.Builder builder = config.webClientBuilder();
        WebClient webClient = builder.build();

        assertNotNull(webClient);
    }
}
