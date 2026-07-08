package com.innowise.paymentservice.mapper;

import com.innowise.paymentservice.dto.PaymentDto;
import com.innowise.paymentservice.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentDto paymentToDto(Payment payment);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Payment dtoToPayment(PaymentDto paymentDto);
}
