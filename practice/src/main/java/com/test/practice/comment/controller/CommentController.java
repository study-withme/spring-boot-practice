package com.test.practice.comment.controller;

import com.test.practice.comment.dto.CommentCreateRequest;
import com.test.practice.comment.dto.CommentResponse;
import com.test.practice.comment.dto.CommentUpdateRequest;
import com.test.practice.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 게시글 하위 댓글 API.
 * <p>
 * 흐름: 클라이언트는 게시글 ID 기준으로 목록(2단 트리) 조회 → 루트에만 {@code replies}가 채워진다.
 */
@RestController
@RequestMapping("/api/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /** 댓글 또는 대댓글 생성. {@link CommentCreateRequest#parentId} 로 단계 구분. */
    @PostMapping
    public CommentResponse create(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        return commentService.createComment(postId, request);
    }

    /** 2단 트리(루트 + replies) 목록. */
    @GetMapping
    public List<CommentResponse> list(@PathVariable Long postId) {
        return commentService.getCommentTree(postId);
    }

    @PutMapping("/{commentId}")
    public CommentResponse update(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return commentService.updateComment(postId, commentId, userId, request);
    }

    @DeleteMapping("/{commentId}")
    public void delete(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(postId, commentId, userId);
    }
}
