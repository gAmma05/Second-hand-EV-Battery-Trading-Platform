package com.example.SWP.repository;

import com.example.SWP.entity.Post;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByUserAndStatus(User user, PostStatus postStatus);

    List<Post> findByStatus(PostStatus postStatus);

    List<Post> findByStatusAndExpiryDateAfterOrderByPostDateDesc(PostStatus postStatus, LocalDateTime now);

    List<Post> findByUserAndStatusNot(User user, PostStatus postStatus);

    List<Post> findByProductTypeAndStatusAndExpiryDateAfterOrderByPostDateDesc(ProductType productType, PostStatus postStatus, LocalDateTime now);

    List<Post> findByPriorityPackageIdNotNullAndStatusAndExpiryDateAfterOrderByPostDateDesc(PostStatus postStatus, LocalDateTime now);

    List<Post> findByIsTrustedTrueAndStatusAndExpiryDateAfterOrderByPostDateDesc(PostStatus postStatus, LocalDateTime now);
}
