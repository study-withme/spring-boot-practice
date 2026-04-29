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


    // 글작성
    @Transactional
    public PostResponse createPost(PostCreateRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .user(user)
                .build();

        Post savedPost = postRepository.save(post);

        return new PostResponse(savedPost);
    }

    public List<PostResponse> getPosts() {
        return postRepository.findAll()
                .stream()
                .map(PostResponse::new)
                .toList();
    }

    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        return new PostResponse(post);
    }

    public List<PostResponse> getPostsByUser(Long userId) {
        return postRepository.findByUserId(userId)
                .stream()
                .map(PostResponse::new)
                .toList();
    }

    //글수정
    @Transactional
    public PostResponse updatePost(Long postId, PostUpdateRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        post.update(request.getTitle(), request.getContent());

        return new PostResponse(post);
    }

    // 글삭제
    @Transactional
    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        post.delete();
    }

}
