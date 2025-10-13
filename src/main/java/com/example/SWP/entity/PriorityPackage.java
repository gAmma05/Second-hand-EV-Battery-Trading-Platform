package com.example.SWP.entity;

import com.example.SWP.enums.PriorityPackageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    PriorityPackageType type;
    int price;
    int durationDays;
    String description;
}
