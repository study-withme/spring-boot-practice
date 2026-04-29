package com.test.practice.post.controller;

import com.test.practice.post.dto.PostCreateRequest;
import com.test.practice.post.dto.PostResponse;
import com.test.practice.post.dto.PostUpdateRequest;
import com.test.practice.post.repository.PostRepository;
import com.test.practice.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 작성
    @PostMapping
    public PostResponse createPost(@RequestBody PostCreateRequest request) {
        return postService.createPost(request);
    }

    // 게시글 전체 조회
    @GetMapping
    public List<PostResponse> getPosts() {
        return postService.getPosts();
    }

    // 게시글 단건 조회
    @GetMapping("/{postId}")
    public PostResponse getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

     // 특정 회원의 게시글 조회
    @GetMapping("/user/{userId}")
    public List<PostResponse> getPostsByUser(@PathVariable Long userId) {
        return postService.getPostsByUser(userId);
    }

    //게시글 수정
    @PutMapping("/{postId}")
    public PostResponse updatePost(
            @PathVariable Long postId,
            @RequestBody PostUpdateRequest request
    ) {
        return postService.updatePost(postId, request);
    }

    //게시글 삭제
    @DeleteMapping("/{postId}")
    public void deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
    }

}
