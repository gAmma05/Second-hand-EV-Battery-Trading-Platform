package com.example.SWP.entity;

import com.example.SWP.enums.DeliveryMethod;
import com.example.SWP.enums.PaymentType;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.PriorityPackageType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    String productType;
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

    String address;
    boolean isTrusted;

    Long priorityPackageId;
    LocalDateTime priorityExpire;

    @Enumerated(EnumType.STRING)
    PostStatus status;
}
