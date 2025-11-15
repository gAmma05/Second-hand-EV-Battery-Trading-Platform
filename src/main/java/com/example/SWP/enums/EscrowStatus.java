package com.example.SWP.enums;

public enum EscrowStatus {
    LOCKED,               // vừa đặt cọc xong, tiền đang giữ
    DISPUTED,             // có complaint
    RELEASED_TO_SELLER,    // seller thắng
    REFUNDED_TO_BUYER,      // buyer thắng
    CLOSED                // escrow hoàn tất
}
