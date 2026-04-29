package com.test.practice.user.service;


import com.test.practice.user.dto.LoginRequest;
import com.test.practice.user.dto.SignupRequest;
import com.test.practice.user.dto.UserResponse;
import com.test.practice.user.entity.Role;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //회원가입 로직
    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {


        //1. 아이디 중복검사
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        //2. 닉네임 중복 검사
        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 3. 회원 엔티티 생성
        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(signupRequest.getPassword())
                .nickname(signupRequest.getNickname())
                .role(Role.USER) // 기본적으로 USER 역할을 부여
                .build();

        // DB 저장
        User savedUser = userRepository.save(user);

        // 5. 응답 DTO 변환
        return UserResponse.from(savedUser);
    }

    //로그인
    public UserResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        /* 패스워드 인코더 도입으로 인한 주석처리
        if (!passwordEncoder.matches()equals(loginRequest.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        } */

        // 패스워드 인코더 도입으로 인한 로그인 검증 로직
        // encode() -> 저장할 때
        // matcheds -> 로그인할 때
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return UserResponse.from(user);
    }

    // 회원 단건 조회
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return UserResponse.from(user);
    }

    // 회원 전체 조회
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponse::from)
                .toList();
    }


    // 회원 수정
    @Transactional
    public UserResponse updateUser(Long id, SignupRequest signupRequest) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.setUsername(signupRequest.getUsername());
        user.setPassword(signupRequest.getPassword());
        user.setNickname(signupRequest.getNickname());

        return UserResponse.from(user);
    }

    // 회원 삭제
    @Transactional
    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        userRepository.delete(user);
    }

}

