package com.example.SWP.service.admin;

import com.example.SWP.dto.response.seller.PostResponse;
import com.example.SWP.entity.Post;
import com.example.SWP.enums.PostStatus;
import com.example.SWP.exception.BusinessException;
import com.example.SWP.mapper.PostMapper;
import com.example.SWP.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class AdminPostService {

    PostRepository postRepository;
    PostMapper postMapper;

    public void approvePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException("Post is not pending approval", 400);
        }

        post.setStatus(PostStatus.POSTED);

        post.setTrusted(true);

        postRepository.save(post);
    }


    public void rejectPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException("Post not found", 404));

        if (post.getStatus() != PostStatus.PENDING) {
            throw new BusinessException("Post is not pending approval", 400);
        }

        post.setStatus(PostStatus.REJECTED);
        postRepository.save(post);
    }

    public List<PostResponse> getPendingPosts() {
        List<Post> posts = postRepository.findByStatus(PostStatus.PENDING);
        return postMapper.toPostResponseList(posts);
    }
}

