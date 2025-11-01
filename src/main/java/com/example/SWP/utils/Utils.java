package com.example.SWP.utils;

import com.example.SWP.entity.User;
import com.example.SWP.enums.TransactionType;
import org.springframework.security.core.Authentication;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Utils {


    public static String generateCode(String prefix) {
        return prefix.toUpperCase() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    public static String generatePaymentDescription(TransactionType transactionType, String code) {
        return "Thanh toán cho " + transactionType.name() + " #" + code;
    }
}
