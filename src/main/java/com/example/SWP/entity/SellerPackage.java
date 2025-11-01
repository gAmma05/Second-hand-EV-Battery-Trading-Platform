package com.example.SWP.entity;

import com.example.SWP.enums.SellerPackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "seller_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    SellerPackageType type;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String description;

    BigDecimal price;

    int postLimit;
}

