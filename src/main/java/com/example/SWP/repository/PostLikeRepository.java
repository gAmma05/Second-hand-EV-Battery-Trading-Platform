package com.example.SWP.repository;


import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostLike;
import com.example.SWP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByBuyerAndPost(User user, Post post);

    Optional<PostLike> findByBuyerAndPost(User user, Post post);

    List<PostLike> findAllByBuyer(User buyer);
}
