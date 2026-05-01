package com.test.practice.user.service;

import com.test.practice.user.dto.LoginRequest;
import com.test.practice.user.dto.SignupRequest;
import com.test.practice.user.dto.UserResponse;
import com.test.practice.user.entity.Role;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

    // 회원 가입 - Create
    @Transactional
    public UserResponse signup(SignupRequest signupRequest) {
        // 1. 요청값 중복 검증 (아이디/닉네임)
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }

        // 2. 비밀번호 암호화 후 엔티티 생성
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = User.builder()
                .username(signupRequest.getUsername())
                .password(encodedPassword)
                .nickname(signupRequest.getNickname())
                .role(Role.USER)
                .build();

        // 3. 저장 결과를 응답 DTO로 변환해 반환
        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    // 로그인 - Read (인증 조회)
    public UserResponse login(LoginRequest loginRequest) {
        // 1. 아이디로 사용자 조회 (없으면 예외)
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        // 2. 비밀번호 일치 여부 검증
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 로그인 대상 사용자 정보를 응답으로 변환
        return UserResponse.from(user);
    }

    // 회원 단건 조회 - Read
    public UserResponse getUser(Long id) {
        // 1. 사용자 단건 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // 2. 엔티티를 응답 DTO로 변환
        return UserResponse.from(user);
    }

    // 회원 전체 조회 - Read
    public List<UserResponse> getAllUsers() {
        // 1. 전체 사용자 조회
        // 2. 각 엔티티를 응답 DTO로 변환
        // 3. 리스트 형태로 반환
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // 회원 정보 수정 - Update
    @Transactional
    public UserResponse updateUser(Long id, SignupRequest signupRequest) {
        // 1. 수정 대상 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 요청값으로 엔티티 상태 변경
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setNickname(signupRequest.getNickname());

        // 3. 변경된 엔티티를 응답 DTO로 변환
        return UserResponse.from(user);
    }

    // 회원 삭제 - Delete
    @Transactional
    public void deleteUser(Long id) {
        // 1. 삭제 대상 사용자 조회
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        // 2. 사용자 삭제 실행
        userRepository.delete(user);
    }
}
