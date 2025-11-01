package com.example.SWP.dto.response.buyer;

import com.example.SWP.dto.response.seller.PostResponse;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostFavoriteResponse {
    Long id;
    PostResponse post;
}
