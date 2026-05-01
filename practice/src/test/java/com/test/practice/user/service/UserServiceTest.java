package com.test.practice.user.service;

import com.test.practice.user.dto.LoginRequest;
import com.test.practice.user.dto.SignupRequest;
import com.test.practice.user.dto.UserResponse;
import com.test.practice.user.entity.Role;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String RAW_PASSWORD = "password12";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword(RAW_PASSWORD);
        request.setNickname("테스터");

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("ENC")
                .nickname("테스터")
                .role(Role.USER)
                .build();

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByNickname("테스터")).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn("ENC");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.signup(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getNickname()).isEqualTo("테스터");

        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원가입 실패 - 중복 아이디")
    void signup_fail_duplicateUsername() {
        SignupRequest request = new SignupRequest();
        request.setUsername("testuser");
        request.setPassword(RAW_PASSWORD);
        request.setNickname("테스터");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 아이디입니다.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(RAW_PASSWORD);

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("ENC")
                .nickname("테스터")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq(RAW_PASSWORD), eq("ENC"))).thenReturn(true);

        UserResponse response = userService.login(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 아이디")
    void login_fail_notFoundUsername() {
        LoginRequest request = new LoginRequest();
        request.setUsername("unknown");
        request.setPassword(RAW_PASSWORD);

        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 아이디입니다.");
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void login_fail_wrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpass1");

        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("ENC")
                .nickname("테스터")
                .role(Role.USER)
                .build();

        when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrongpass1"), eq("ENC"))).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("회원 단건 조회 성공")
    void getUser_success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("ENC")
                .nickname("테스터")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        UserResponse response = userService.getUser(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("회원 전체 조회 성공")
    void getAllUsers_success() {
        User user1 = User.builder()
                .id(1L)
                .username("user1")
                .password("ENC")
                .nickname("유저1")
                .role(Role.USER)
                .build();

        User user2 = User.builder()
                .id(2L)
                .username("user2")
                .password("ENC")
                .nickname("유저2")
                .role(Role.USER)
                .build();

        when(userRepository.findAll())
                .thenReturn(List.of(user1, user2));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getUsername()).isEqualTo("user1");
        assertThat(responses.get(1).getUsername()).isEqualTo("user2");
    }

    @Test
    @DisplayName("회원 수정 성공")
    void updateUser_success() {
        User user = User.builder()
                .id(1L)
                .username("old")
                .password("ENC_OLD")
                .nickname("기존닉")
                .role(Role.USER)
                .build();

        SignupRequest request = new SignupRequest();
        request.setUsername("new");
        request.setPassword(RAW_PASSWORD);
        request.setNickname("새닉네임");

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn("ENC_NEW");

        UserResponse response = userService.updateUser(1L, request);

        assertThat(response.getUsername()).isEqualTo("new");
        assertThat(response.getNickname()).isEqualTo("새닉네임");

        assertThat(user.getUsername()).isEqualTo("new");
        assertThat(user.getPassword()).isEqualTo("ENC_NEW");
        assertThat(user.getNickname()).isEqualTo("새닉네임");
    }

    @Test
    @DisplayName("회원 삭제 성공")
    void deleteUser_success() {
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .password("ENC")
                .nickname("테스터")
                .role(Role.USER)
                .build();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }
}
