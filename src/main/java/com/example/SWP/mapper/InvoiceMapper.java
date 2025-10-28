package com.example.SWP.mapper;

import com.example.SWP.dto.response.buyer.InvoiceResponse;
import com.example.SWP.entity.Invoice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(source = "contract.id", target = "contractId")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    List<InvoiceResponse> toInvoiceResponseList(List<Invoice> invoices);

}
