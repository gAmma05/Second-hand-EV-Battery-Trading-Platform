package com.example.SWP.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAvatarRequest {
    @NotBlank(message = "Avatar URL cannot be blank")
    String avatar;
}
