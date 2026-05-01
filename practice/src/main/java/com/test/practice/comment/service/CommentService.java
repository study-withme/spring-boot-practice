package com.test.practice.comment.service;

import com.test.practice.comment.dto.CommentCreateRequest;
import com.test.practice.comment.dto.CommentResponse;
import com.test.practice.comment.dto.CommentUpdateRequest;
import com.test.practice.comment.entity.Comment;
import com.test.practice.comment.repository.CommentRepository;
import com.test.practice.post.entity.Post;
import com.test.practice.post.repository.PostRepository;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // Create
    @Transactional
    public CommentResponse createComment(Long postId, CommentCreateRequest request) {
        // 1. 삭제되지 않은 게시글 단건 조회
        Post post = postRepository.findByIdAndDeleteTimeIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 작성자(유저) 단건 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 3. parentId가 있으면 부모 댓글 조회 후, 같은 게시글·루트 댓글인지 검증(최대 2단)
        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findByIdAndDeleteTimeIsNull(request.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("다른 게시글의 댓글에는 답글을 달 수 없습니다.");
            }
            if (parent.getParent() != null) {
                throw new IllegalArgumentException("대댓글에는 답글을 달 수 없습니다. (최대 2단계)");
            }
        }

        // 4. 요청값으로 Comment 엔티티 생성
        Comment comment = Comment.builder()
                .content(request.getContent())
                .post(post)
                .user(user)
                .parent(parent)
                .nickname(user.getNickname())
                .build();

        // 5. 저장 후 응답 DTO로 반환
        Comment saved = commentRepository.save(comment);
        return new CommentResponse(saved);
    }

    // Read
    public List<CommentResponse> getCommentTree(Long postId) {
        // 1. 삭제되지 않은 게시글 존재 여부 조회
        postRepository.findByIdAndDeleteTimeIsNull(postId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        // 2. 해당 게시글의 미삭제 댓글 전부 조회(작성순)
        List<Comment> all = commentRepository.findByPost_IdAndDeleteTimeIsNullOrderByCreateTimeAsc(postId);

        // 3. 대댓글만 부모 id 기준으로 그룹핑(루트별 replies 조립용)
        Map<Long, List<Comment>> repliesByParentId = all.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(
                        c -> c.getParent().getId(),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)));

        // 4. 루트 댓글 순서대로 CommentResponse(자식 replies 포함) 리스트로 반환
        return all.stream()
                .filter(c -> c.getParent() == null)
                .map(root -> {
                    List<Comment> children = repliesByParentId.getOrDefault(root.getId(), List.of());
                    List<CommentResponse> replyDtos = children.stream()
                            .map(CommentResponse::new)
                            .toList();
                    return new CommentResponse(root, replyDtos);
                })
                .toList();
    }

    // Update
    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, Long userId, CommentUpdateRequest request) {
        // 1. 미삭제 댓글 단건 조회
        Comment comment = commentRepository.findByIdAndDeleteTimeIsNull(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        // 2. URL postId와 댓글이 속한 게시글 일치 여부 검증
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }
        // 3. 요청 userId가 작성자인지 검증
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 수정할 수 있습니다.");
        }
        // 4. 엔티티 내용 수정 후 응답 DTO로 반환
        comment.update(request.getContent());
        return new CommentResponse(comment);
    }

    // Delete
    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        // 1. 미삭제 댓글 단건 조회
        Comment comment = commentRepository.findByIdAndDeleteTimeIsNull(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));
        // 2. URL postId와 댓글이 속한 게시글 일치 여부 검증
        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("해당 게시글의 댓글이 아닙니다.");
        }
        // 3. 요청 userId가 작성자인지 검증
        if (!comment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("댓글 작성자만 삭제할 수 있습니다.");
        }
        // 4. 소프트 삭제(deleteTime 설정)
        comment.markDeleted();
    }
}
