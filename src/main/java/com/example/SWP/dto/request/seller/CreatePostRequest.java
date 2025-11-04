package com.example.SWP.dto.request.seller;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.ProductType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePostRequest {

    @NotNull(message = "Loại sản phẩm là bắt buộc")
    ProductType productType;

    @NotBlank(message = "Tiêu đề là bắt buộc")
    @Size(max = 200, message = "Tiêu đề không được vượt quá 200 ký tự")
    String title;

    @NotBlank(message = "Mô tả là bắt buộc")
    @Size(max = 2000, message = "Mô tả không được vượt quá 2000 ký tự")
    String description;

    @Positive(message = "Giá phải lớn hơn 0")
    BigDecimal price;

    Long priorityPackageId;

    @NotEmpty(message = "Cần chọn ít nhất một phương thức giao hàng")
    Set<DeliveryMethod> deliveryMethods;

    @NotEmpty(message = "Cần chọn ít nhất một phương thức thanh toán")
    Set<PaymentType> paymentTypes;

    @NotEmpty(message = "Phải có ít nhất một hình ảnh")
    List<@NotBlank(message = "Đường dẫn hình ảnh không được để trống") String> images;

    @NotNull(message = "Khối lượng là bắt buộc")
    @Positive(message = "Khối lượng phải lớn hơn 0")
    Integer weight;

    boolean wantsTrustedLabel;

    String vehicleBrand;
    String model;
    Integer yearOfManufacture;
    String color;
    Integer mileage;

    String batteryType;
    Integer capacity;
    String voltage;
    String batteryBrand;
}
