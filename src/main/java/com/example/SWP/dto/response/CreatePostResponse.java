package com.example.SWP.dto.response;

import com.example.SWP.entity.Post;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class CreatePostResponse {
    Post post;
    String paymentUrl;
}

