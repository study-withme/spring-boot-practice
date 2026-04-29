package com.test.practice.user.controller;

import com.test.practice.user.dto.LoginRequest;
import com.test.practice.user.dto.SignupRequest;
import com.test.practice.user.dto.UserResponse;
import com.test.practice.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    //회원가입 컨트롤러
    @GetMapping("/signup")
    public UserResponse signup(@RequestBody SignupRequest signupRequest) {
        return userService.signup(signupRequest);
    }

    // 로그인 컨트롤러
    @PostMapping("/login")
    public UserResponse login(@RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    //회원 전체조회
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    //회원 단건 조회
    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getUser(id);
    }

    //회원 수정
    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @RequestBody SignupRequest signupRequest
    ) {
        return userService.updateUser(id, signupRequest);
    }

    //회원 삭제
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
