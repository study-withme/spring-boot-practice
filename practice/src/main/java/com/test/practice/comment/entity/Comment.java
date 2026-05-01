package com.test.practice.comment.entity;

import com.test.practice.post.entity.Post;
import com.test.practice.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 게시글에 달리는 댓글 엔티티.
 * <p>
 * 2단 구조: {@code parent == null} 이면 루트(1단), {@code parent != null} 이면 대댓글(2단).
 * 부모의 부모가 있는 댓글에는 답글을 달 수 없도록 서비스에서 검증한다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 300)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    /**
     * 상위 댓글. null이면 게시글에 직접 달린 루트 댓글.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /** 작성 시점 닉네임 스냅샷(이후 유저 닉네임 변경과 무관하게 표시용). */
    @Column(nullable = false)
    private String nickname;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;

    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }

    public void update(String content) {
        this.content = content;
    }

    /** 소프트 삭제: 목록 조회 시 제외된다. */
    public void markDeleted() {
        this.deleteTime = LocalDateTime.now();
    }
}
