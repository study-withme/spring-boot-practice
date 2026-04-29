package com.test.practice.user.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String username;
    private String password;
    private String nickname;

    // 2. 사이트 역할 추가 (enum)
    private String role;
}
