package com.test.practice.post.service;


import com.test.practice.post.dto.PostCreateRequest;
import com.test.practice.post.dto.PostResponse;
import com.test.practice.post.dto.PostUpdateRequest;
import com.test.practice.post.entity.Post;
import com.test.practice.post.repository.PostRepository;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    // 게시글 작성 - Create
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {
        // 1. 작성자(유저) 존재 여부 확인
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        // 2. 요청값으로 게시글 엔티티 생성
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        // 3. 저장 후 응답 DTO로 반환
        Post savedPost = postRepository.save(post);

        return new PostResponse(savedPost);
    }

    // 게시글 목록 조회 - Read
    public List<PostResponse> getPosts() {
        // 1. 삭제되지 않은 게시글 목록 조회
        // 2. 각 게시글을 응답 DTO로 변환
        // 3. 리스트 형태로 반환
        return postRepository.findByDeleteTimeIsNull()
                .stream()
                .map(PostResponse::new)
                .toList();
    }

    // 게시글 단건 조회 - Read
    public PostResponse getPost(Long postId) {
        // 1. 삭제되지 않은 게시글 단건 조회
        Post post = postRepository.findByIdAndDeleteTimeIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 엔티티를 응답 DTO로 변환
        return new PostResponse(post);
    }

    // 사용자별 게시글 조회 - Read
    public List<PostResponse> getPostsByUser(Long userId) {
        // 1. 특정 사용자 + 삭제되지 않은 게시글 목록 조회
        // 2. 각 게시글을 응답 DTO로 변환
        // 3. 리스트 형태로 반환
        return postRepository.findByUser_IdAndDeleteTimeIsNull(userId)
                .stream()
                .map(PostResponse::new)
                .toList();
    }

    // 게시글 수정 - Update
    @Transactional
    public PostResponse updatePost(Long userId, Long postId, PostUpdateRequest request) {
        // 1. 수정 대상 게시글 조회
        Post post = postRepository.findByIdAndDeleteTimeIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 요청 사용자가 작성자인지 권한 검증
        if (!post.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("게시글 작성자만 수정할 수 있습니다.");
        }

        // 3. 게시글 내용 수정 후 응답 DTO로 반환
        post.update(request.getTitle(), request.getContent());

        return new PostResponse(post);
    }

    // 게시글 삭제 - Delete
    @Transactional
    public void deletePost(Long postId) {
        // 1. 삭제 대상 게시글 조회
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 소프트 삭제 처리(deleteTime 설정)
        post.delete();
    }

}
