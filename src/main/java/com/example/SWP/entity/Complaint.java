package com.example.SWP.entity;

import com.example.SWP.enums.ComplaintStatus;
import com.example.SWP.enums.ComplaintType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "complaints")
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Complaint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    Order order;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User complainer;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    User againUser;

    @Enumerated(EnumType.STRING)
    ComplaintType type;

    @Column(columnDefinition = "TEXT")
    String description;

    String evidenceUrls;

    String resolutionNotes;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    ComplaintStatus status;

}
