package com.example.SWP.entity;

import com.example.SWP.enums.PriorityPackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "priority_packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriorityPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(255)")
    PriorityPackageType type;

    BigDecimal price;

    int durationDays;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String description;
}
