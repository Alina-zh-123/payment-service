package com.innowise.paymentservice.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
public class MockRandomController {
    @GetMapping("/integers")
    public Integer getMockInteger() {
        return ThreadLocalRandom.current().nextInt(1, 101);
    }
}
