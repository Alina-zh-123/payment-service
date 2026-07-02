package com.innowise.paymentservice.client;

import org.springframework.stereotype.Component;

@Component
public class RandomClientFallback implements RandomClient {
    @Override
    public Integer getInteger() {
        throw new IllegalStateException(
                "Random service unavailable!"
        );
    }
}
