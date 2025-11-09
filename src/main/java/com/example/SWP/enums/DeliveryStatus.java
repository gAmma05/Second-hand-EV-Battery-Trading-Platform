package com.example.SWP.enums;

import lombok.Getter;

@Getter
public enum DeliveryStatus {
    PREPARING("ĐANG CHUẨN BỊ"),
    READY("SẴN SÀNG GIAO"),
    DELIVERING("ĐANG GIAO HÀNG"),
    PICKUP_PENDING("CHỜ LẤY HÀNG"),
    DELIVERED("ĐÃ GIAO THÀNH CÔNG"),
    RECEIVED("ĐÃ NHẬN HÀNG");

    private final String vietnameseName;

    DeliveryStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

}
