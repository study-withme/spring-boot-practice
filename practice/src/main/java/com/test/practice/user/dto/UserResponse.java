package com.test.practice.user.dto;

import com.test.practice.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String username;
    private String nickname;

    public static UserResponse from(User user) {
        return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getNickname()
    );
    }

}
