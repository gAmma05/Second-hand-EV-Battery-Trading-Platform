package com.example.SWP.enums;

public enum EscrowStatus {
    LOCKED,               // vừa đặt cọc xong, tiền đang giữ
    DISPUTED,             // có complaint
    RELEASE_TO_SELLER,    // seller thắng
    REFUND_TO_BUYER,      // buyer thắng
    CLOSED                // escrow hoàn tất
}
