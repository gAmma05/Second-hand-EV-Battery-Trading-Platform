package com.example.SWP.entity;

import com.example.SWP.enums.*;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    User user;

    ProductType productType;
    String title;
    String description;

    BigDecimal price;
    double suggestPrice;

    LocalDateTime postDate;
    LocalDateTime updateDate;
    LocalDateTime expiryDate;

    int viewCount;
    int likeCount;

    @ElementCollection(targetClass = DeliveryMethod.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "post_delivery_methods", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "delivery_method")
    Set<DeliveryMethod> deliveryMethods = new HashSet<>();

    @ElementCollection(targetClass = PaymentType.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "post_payment_types", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "payment_type")
    Set<PaymentType> paymentTypes = new HashSet<>();

    boolean isTrusted;

    Long priorityPackageId;
    LocalDateTime priorityExpire;

    @Enumerated(EnumType.STRING)
    PostStatus status;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "image_order")
    List<PostImage> images = new ArrayList<>();

    String vehicleBrand;
    String model;
    Integer yearOfManufacture;
    String color;
    Integer mileage;

    String batteryType;
    Integer capacity;
    String voltage;
    String batteryBrand;

    Integer weight;
}
