package com.example.SWP.repository;

import com.example.SWP.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Feedback findByPost_Id(Long postId);
}
