package com.AcolheSP.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient backendWebClient(
        @Value("${backend.url}") String backendUrl) {

            return WebClient.builder().baseUrl(backendUrl).build();
        }
}
