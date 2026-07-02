package com.innowise.paymentservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "custom-random-service",
        url = "${RANDOM_SERVICE_URL}",
        fallback = RandomClientFallback.class
)
public interface RandomClient {
    @GetMapping("/integers")
    Integer getInteger();
}
