package com.innowise.paymentservice.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.innowise.paymentservice.client.RandomClient;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@WireMockTest(httpPort = 0)
public class PaymentIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private PaymentMapper paymentMapper;
    private PaymentDto paymentDto;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RandomClient randomClient;

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");
    @Container
    static KafkaContainer kafkaContainer = new KafkaContainer("confluentinc/cp-kafka:7.5.0");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getConnectionString);
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);

        registry.add("RANDOM_SERVICE_URL", () -> "http://localhost:${wiremock.server.port}");
    }

    @BeforeEach
    void setup(){
        paymentDto = new PaymentDto();
        paymentDto.setStatus(PaymentStatus.PENDING);
        paymentDto.setId("1");
        paymentDto.setAmount(BigDecimal.valueOf(673287.23));
        paymentDto.setUserId(1L);
        paymentDto.setOrderId(1L);
        paymentDto.setCreatedAt(LocalDateTime.parse("2026-07-03T10:00"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPayment_shouldCreatePaymentAndReturnResponse() throws Exception {
        stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/integers"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("2")
                        .withStatus(200)));

        mockMvc.perform(post("/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentDto)))
                .andExpect(status().isAccepted());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_shouldReturnPayment() throws Exception {
        paymentRepository.save(paymentMapper.dtoToPayment(paymentDto));

        mockMvc.perform(get("/payments/{id}", "1"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.id").value("1"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.amount").value(673287.23))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.userId").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.orderId").value(1));

        Assertions.assertTrue(paymentRepository.findById("1").isPresent());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPaymentById_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/payments/{id}", 1234L))
                .andExpect(status().isNotFound())
                .andExpect((ResultMatcher) content().string(containsString("Payment is not found!")));
    }
}
