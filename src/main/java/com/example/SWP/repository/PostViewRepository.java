package com.example.SWP.repository;


import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostView;
import com.example.SWP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewRepository extends JpaRepository<PostView, Long> {
    boolean existsByBuyerAndPost(User buyer, Post post);
}
