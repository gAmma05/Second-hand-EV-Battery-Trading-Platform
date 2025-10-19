package com.example.SWP.service.publics;

import com.example.SWP.entity.Post;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.repository.PostRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    public List<Post> getAllPosts() {
        LocalDateTime now = LocalDateTime.now();

        List<Post> allPosts = postRepository.findByStatusAndExpiryDateAfterOrderByPostDateDesc(PostStatus.POSTED, now);

        List<Post> priorityPosts = allPosts.stream()
                .filter(p -> p.getPriorityPackageId() != null)
                .sorted(Comparator.comparing(Post::getPostDate).reversed())
                .toList();

        List<Post> trustedPosts = allPosts.stream()
                .filter(p -> p.getPriorityPackageId() == null && p.isTrusted())
                .sorted(Comparator.comparing(Post::getPostDate).reversed())
                .toList();

        List<Post> normalPosts = allPosts.stream()
                .filter(p -> p.getPriorityPackageId() == null && !p.isTrusted())
                .sorted(Comparator.comparing(Post::getPostDate).reversed())
                .toList();

        // Gộp lại theo thứ tự: priority → trusted → normal
        List<Post> sortedPosts = new ArrayList<>();
        sortedPosts.addAll(priorityPosts);
        sortedPosts.addAll(trustedPosts);
        sortedPosts.addAll(normalPosts);

        return sortedPosts;
    }

}
