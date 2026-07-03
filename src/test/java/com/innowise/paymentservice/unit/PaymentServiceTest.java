package com.innowise.paymentservice.unit;

import com.innowise.paymentservice.client.RandomClient;
import com.innowise.paymentservice.dto.PaymentAmountDto;
import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.entity.Payment;
import com.innowise.paymentservice.entity.PaymentStatus;
import com.innowise.paymentservice.exception.PaymentException;
import com.innowise.paymentservice.kafka.PaymentCompletedEvent;
import com.innowise.paymentservice.mapper.PaymentMapper;
import com.innowise.paymentservice.repository.PaymentRepository;
import com.innowise.paymentservice.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private RandomClient randomClient;
    @Mock
    private PaymentServiceImpl self;
    @Mock
    private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private Payment payment1;
    private PaymentDto paymentDto1;
    private Payment payment2;
    private PaymentDto paymentDto2;
    private PaymentAmountDto paymentAmountDto1;
    private PaymentAmountDto paymentAmountDto2;

    @BeforeEach
    public void setUp() {
        payment1 = new Payment();
        payment1.setStatus(PaymentStatus.PENDING);
        payment1.setId("1");
        payment1.setAmount(BigDecimal.valueOf(673287.23));
        payment1.setUserId(1L);
        payment1.setOrderId(1L);
        payment1.setCreatedAt(LocalDateTime.parse("2026-07-03T10:00"));

        payment2 = new Payment();
        payment2.setStatus(PaymentStatus.PENDING);
        payment2.setId("2");
        payment2.setAmount(BigDecimal.valueOf(13312.00));
        payment2.setUserId(2L);
        payment2.setOrderId(2L);
        payment2.setCreatedAt(LocalDateTime.parse("2026-06-30T19:00"));

        paymentDto1 = new PaymentDto();
        paymentDto1.setStatus(PaymentStatus.PENDING);
        paymentDto1.setId("1");
        paymentDto1.setAmount(BigDecimal.valueOf(673287.23));
        paymentDto1.setUserId(1L);
        paymentDto1.setOrderId(1L);
        paymentDto1.setCreatedAt(LocalDateTime.parse("2026-07-03T10:00"));

        paymentDto2 = new PaymentDto();
        paymentDto2.setStatus(PaymentStatus.PENDING);
        paymentDto2.setId("2");
        paymentDto2.setAmount(BigDecimal.valueOf(13312.00));
        paymentDto2.setUserId(2L);
        paymentDto2.setOrderId(2L);
        paymentDto2.setCreatedAt(LocalDateTime.parse("2026-06-30T19:00"));

        paymentAmountDto1 = new PaymentAmountDto();
        paymentAmountDto1.setTotalAmount(BigDecimal.valueOf(673287.23));

        paymentAmountDto2 = new PaymentAmountDto();
        paymentAmountDto2.setTotalAmount(BigDecimal.valueOf(686599.23));

        Jwt jwt = mock(Jwt.class);
        lenient().when(jwt.getClaimAsString("user_id")).thenReturn("1");
        lenient().when(jwt.getClaimAsString("role")).thenReturn("ADMIN");

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getPrincipal()).thenReturn(jwt);

        SecurityContext context = mock(SecurityContext.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(context);
    }

    @Test
    void createPayment_shouldCreatePayment() {
        when(paymentRepository.save(payment1)).thenReturn(payment1);
        when(paymentMapper.dtoToPayment(paymentDto1)).thenReturn(payment1);
        when(paymentMapper.paymentToDto(payment1)).thenReturn(paymentDto1);
        doNothing().when(self).processPayment("1");

        PaymentDto paymentDto = paymentService.createPayment(paymentDto1);
        assertEquals(paymentDto1, paymentDto);

        verify(paymentRepository).save(payment1);
        verify(paymentMapper).dtoToPayment(paymentDto1);
        verify(paymentMapper).paymentToDto(payment1);
    }

    @Test
    void createPayment_shouldThrowPaymentException() {
        when(paymentMapper.dtoToPayment(paymentDto1)).thenReturn(payment1);
        when(paymentRepository.save(payment1)).thenThrow(new PaymentException("Payment processing error!"));

        assertThrows(PaymentException.class, () -> paymentService.createPayment(paymentDto1));

        verify(paymentRepository).save(payment1);
    }

    @Test
    void getPaymentById_shouldReturnPayment() {
        when(paymentRepository.findById("1")).thenReturn(Optional.of(payment1));
        when(paymentMapper.paymentToDto(payment1)).thenReturn(paymentDto1);

        PaymentDto paymentDto = paymentService.getPaymentById("1");
        assertEquals(paymentDto1, paymentDto);

        verify(paymentRepository).findById("1");
        verify(paymentMapper).paymentToDto(payment1);
    }

    @Test
    void getPaymentById_shouldThrowPaymentException() {
        when(paymentRepository.findById("1234")).thenReturn(Optional.empty());

        Exception exception = assertThrows(PaymentException.class, () -> {
            paymentService.getPaymentById("1234");
        });
        assertEquals("Payment is not found!", exception.getMessage());

        verify(paymentRepository).findById("1234");
    }

    @Test
    void getPaymentsWithFilter_shouldReturnPayments() {
        List<Payment> paymentList1 = new ArrayList<>();
        paymentList1.add(payment1);
        paymentList1.add(payment2);
        when(paymentRepository.findPaymentBy(1L, 1L, PaymentStatus.PENDING.name()))
                .thenReturn(paymentList1);
        when(paymentMapper.paymentToDto(payment1)).thenReturn(paymentDto1);
        when(paymentMapper.paymentToDto(payment2)).thenReturn(paymentDto2);

        List<PaymentDto> paymentDtoList2 = paymentService.getPaymentsWithFilter(1L, 1L, PaymentStatus.PENDING.name());
        assertEquals(2, paymentDtoList2.size());
        assertEquals(paymentDto1, paymentDtoList2.getFirst());
        assertEquals(paymentDto2, paymentDtoList2.getLast());

        verify(paymentRepository).findPaymentBy(1L, 1L, PaymentStatus.PENDING.name());
        verify(paymentMapper).paymentToDto(payment1);
        verify(paymentMapper).paymentToDto(payment2);
    }

    @Test
    void getPaymentAmountByUserId_shouldReturnPaymentAmount() {
        when(paymentRepository.findPaymentAmountByUserId(
                1L,
                LocalDateTime.parse("2026-07-02T10:00"),
                LocalDateTime.parse("2026-07-04T10:00"))).thenReturn(paymentAmountDto1);

        PaymentAmountDto paymentAmountDto = paymentService.getPaymentAmountByUserId(1L, LocalDateTime.parse("2026-07-02T10:00"), LocalDateTime.parse("2026-07-04T10:00"));
        assertEquals(paymentAmountDto1, paymentAmountDto);

        verify(paymentRepository).findPaymentAmountByUserId(
                1L,
                LocalDateTime.parse("2026-07-02T10:00"),
                LocalDateTime.parse("2026-07-04T10:00"));
    }

    @Test
    void getAllPaymentAmount_shouldReturnPaymentAmount() {
        when(paymentRepository.findAllPaymentAmount(
                LocalDateTime.parse("2026-07-02T10:00"),
                LocalDateTime.parse("2026-07-04T10:00"))).thenReturn(paymentAmountDto2);

        PaymentAmountDto paymentAmountDto = paymentService.getAllPaymentAmount(LocalDateTime.parse("2026-07-02T10:00"), LocalDateTime.parse("2026-07-04T10:00"));
        assertEquals(paymentAmountDto2, paymentAmountDto);

        verify(paymentRepository).findAllPaymentAmount(
                LocalDateTime.parse("2026-07-02T10:00"),
                LocalDateTime.parse("2026-07-04T10:00"));

    }
}