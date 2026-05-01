package com.test.practice.comment.dto;

import com.test.practice.comment.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * API 응답용 댓글 DTO.
 * 루트 댓글은 {@link #replies}에 대댓글만 담고, 대댓글 노드의 {@code replies}는 비어 있다(최대 2단).
 */
@Getter
public class CommentResponse {

    private final Long id;
    private final String content;
    private final Long userId;
    private final Long postId;
    private final String nickname;
    /** null이면 루트 댓글. */
    private final Long parentId;
    /** 0: 루트, 1: 대댓글. */
    private final int depth;

    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    private final List<CommentResponse> replies;

    /** 대댓글 등 자식 없이 단일 노드로 직렬화할 때 사용. */
    public CommentResponse(Comment comment) {
        this(comment, Collections.emptyList());
    }

    /** 루트 댓글 + 그 아래 대댓글 목록을 한 번에 담을 때 사용. */
    public CommentResponse(Comment comment, List<CommentResponse> replies) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.userId = comment.getUser().getId();
        this.postId = comment.getPost().getId();
        this.nickname = comment.getNickname();
        this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        this.depth = comment.getParent() == null ? 0 : 1;
        this.createTime = comment.getCreateTime();
        this.updateTime = comment.getUpdateTime();
        this.replies = replies;
    }
}
