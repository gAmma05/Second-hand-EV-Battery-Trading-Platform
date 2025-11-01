package com.example.SWP.service.publics;

import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.entity.PostView;
import com.example.SWP.entity.User;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.enums.ProductType;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.PostMapper;
import com.example.SWP.repository.PostRepository;
import com.example.SWP.repository.PostViewRepository;
import com.example.SWP.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicPostService {

    PostRepository postRepository;
    PostMapper postMapper;
    PostViewRepository postViewRepository;
    UserRepository userRepository;

    public PostResponse getPostById(Authentication authentication, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName())) {
            String email = authentication.getName();

            User user = userRepository.findByEmail(email).orElse(null);

            boolean viewed = postViewRepository.existsByBuyerAndPost(user, post);

            if (!viewed) {
                post.setViewCount(post.getViewCount() + 1);
                postRepository.save(post);

                PostView postView = new PostView();
                postView.setBuyer(user);
                postView.setPost(post);
                postViewRepository.save(postView);
            }
        }

        return postMapper.toPostResponse(post);
    }

    private List<PostResponse> sortPostsByPriorityTrustedNormal(List<Post> posts) {
        LocalDateTime now = LocalDateTime.now();

        List<Post> priorityPosts = new ArrayList<>();
        List<Post> trustedPosts = new ArrayList<>();
        List<Post> normalPosts = new ArrayList<>();

        posts.forEach(p -> {
            // Chỉ coi là priority nếu có gói và chưa hết hạn
            if (p.getPriorityPackageId() != null && p.getPriorityExpire() != null && p.getPriorityExpire().isAfter(now)) {
                priorityPosts.add(p);
            }
            // Trusted nếu không phải priority nhưng trusted
            else if (p.isTrusted()) {
                trustedPosts.add(p);
            }
            // Còn lại là normal
            else {
                normalPosts.add(p);
            }
        });

        Comparator<Post> byDateDesc = Comparator.comparing(Post::getPostDate).reversed();
        priorityPosts.sort(byDateDesc);
        trustedPosts.sort(byDateDesc);
        normalPosts.sort(byDateDesc);

        List<Post> sortedPosts = new ArrayList<>();
        sortedPosts.addAll(priorityPosts);
        sortedPosts.addAll(trustedPosts);
        sortedPosts.addAll(normalPosts);


        return postMapper.toPostResponseList(sortedPosts);
    }


    // Lấy tất cả post
    public List<PostResponse> getAllPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> allPosts = postRepository.findByStatusAndExpiryDateAfterOrderByPostDateDesc(PostStatus.POSTED, now);
        return sortPostsByPriorityTrustedNormal(allPosts);
    }

    // Lấy post theo ProductType
    public List<PostResponse> getPostsByProductType(ProductType productType) {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository
                .findByProductTypeAndStatusAndExpiryDateAfterOrderByPostDateDesc(productType, PostStatus.POSTED, now);
        return sortPostsByPriorityTrustedNormal(posts);
    }

    // Lấy tất cả bài ưu tiên
    public List<PostResponse> getPriorityPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.findByPriorityPackageIdNotNullAndStatusAndExpiryDateAfterOrderByPostDateDesc(
                PostStatus.POSTED, now
        );
        return sortPostsByPriorityTrustedNormal(posts);
    }

    // Lấy tất cả bài trusted
    public List<PostResponse> getTrustedPosts() {
        LocalDateTime now = LocalDateTime.now();
        List<Post> posts = postRepository.findByIsTrustedTrueAndStatusAndExpiryDateAfterOrderByPostDateDesc(
                PostStatus.POSTED, now
        );
        return sortPostsByPriorityTrustedNormal(posts);
    }
}

