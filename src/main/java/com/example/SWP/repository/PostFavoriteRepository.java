package com.example.SWP.repository;

import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostFavorite;
import com.example.SWP.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Integer> {
    boolean existsByBuyerAndPost(User user, Post post);

    Optional<PostFavorite> findByBuyerAndPost(User user, Post post);

    List<PostFavorite> findAllByBuyer(User user);
}
