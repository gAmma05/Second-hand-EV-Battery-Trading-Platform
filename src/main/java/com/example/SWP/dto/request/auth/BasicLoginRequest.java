<<<<<<<< HEAD:src/main/java/com/example/SWP/dto/request/UpdateUserRequest.java
package com.example.SWP.dto.request;
========
package com.example.SWP.dto.request.auth;
>>>>>>>> origin/locbe:src/main/java/com/example/SWP/dto/request/auth/BasicLoginRequest.java

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
<<<<<<<< HEAD:src/main/java/com/example/SWP/dto/request/UpdateUserRequest.java
public class UpdateUserRequest {
    String fullName;
    String phone;
    String address;
    String avatar;
========
public class BasicLoginRequest {
    String email;
    String password;
>>>>>>>> origin/locbe:src/main/java/com/example/SWP/dto/request/auth/BasicLoginRequest.java
}
