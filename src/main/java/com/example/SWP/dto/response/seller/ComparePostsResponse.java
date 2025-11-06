package com.example.SWP.dto.response.seller;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ComparePostsResponse {
    PostResponse post1;
    PostResponse post2;
    String comparisonResult;
}
