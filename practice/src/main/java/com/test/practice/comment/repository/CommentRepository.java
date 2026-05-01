package com.test.practice.comment.repository;

import com.test.practice.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 특정 게시글의 삭제되지 않은 댓글 전부(루트+대댓글), 작성순.
     * 서비스에서 parent 기준으로 트리를 조립한다.
     */
    List<Comment> findByPost_IdAndDeleteTimeIsNullOrderByCreateTimeAsc(Long postId);

    Optional<Comment> findByIdAndDeleteTimeIsNull(Long commentId);
}
