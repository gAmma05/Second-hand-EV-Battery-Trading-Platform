package com.example.SWP.entity;

import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.ComplaintType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "complaints")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    ComplaintType type;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String description;

    @OneToMany(mappedBy = "complaint", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "image_order")
    List<ComplaintImage> complaintImages;

    @Column(columnDefinition = "NVARCHAR(1000)")
    String resolutionNotes;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(50)")
    ComplaintStatus status;

}
