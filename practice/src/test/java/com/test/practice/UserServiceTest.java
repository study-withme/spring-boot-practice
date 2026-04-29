package com.test.practice.post.service;

import com.test.practice.post.dto.PostCreateRequest;
import com.test.practice.post.dto.PostResponse;
import com.test.practice.post.dto.PostUpdateRequest;
import com.test.practice.post.entity.Post;
import com.test.practice.post.repository.PostRepository;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest  {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostService postService;

    private User user;
    private Post post;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("testuser")
                .password("1234")
                .build();

        post = Post.builder()
                .id(1L)
                .title("기존 제목")
                .content("기존 내용")
                .user(user)
                .viewCount(0L)
                .build();
    }

    @Test
    void 게시글_작성_성공() {
        PostCreateRequest request = new PostCreateRequest();
        request.setUserId(1L);
        request.setTitle("새 제목");
        request.setContent("새 내용");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostResponse response = postService.createPost(request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("기존 제목");
        assertThat(response.getContent()).isEqualTo("기존 내용");
        assertThat(response.getUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    void 게시글_단건조회_성공() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.getPost(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("기존 제목");
        assertThat(response.getContent()).isEqualTo("기존 내용");

        verify(postRepository).findById(1L);
    }

    @Test
    void 게시글_수정_성공() {
        PostUpdateRequest request = new PostUpdateRequest();
        request.setTitle("수정 제목");
        request.setContent("수정 내용");

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostResponse response = postService.updatePost(1L, request);

        assertThat(response.getTitle()).isEqualTo("수정 제목");
        assertThat(response.getContent()).isEqualTo("수정 내용");

        verify(postRepository).findById(1L);
    }

    @Test
    void 게시글_삭제_성공() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        assertThat(post.getDeleteTime()).isNotNull();

        verify(postRepository).findById(1L);
    }
}