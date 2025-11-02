package com.example.SWP.dto.response.buyer;

import com.example.SWP.enums.InvoiceStatus;
import com.example.SWP.enums.PaymentMethod;
import com.example.SWP.enums.PaymentType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
    Long id;
    Long contractId;
    String invoiceNumber;
    BigDecimal totalPrice;
    LocalDateTime createdAt;
    LocalDateTime dueDate;
    LocalDateTime paidAt;
    InvoiceStatus status;
}
