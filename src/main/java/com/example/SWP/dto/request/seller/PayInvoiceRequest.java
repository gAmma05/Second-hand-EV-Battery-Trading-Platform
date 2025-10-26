package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class PayInvoiceRequest {
    @NotNull(message = "Invoice ID must not be null")
    Long invoiceId;

    @NotNull(message = "Payment method must not be null")
    PaymentMethod paymentMethod;
}
