package com.example.SWP.repository;

import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserAndStatus(User user, PostStatus postStatus);

    List<Post> findByUser(User user);

    List<Post> findByStatus(PostStatus postStatus);
}
