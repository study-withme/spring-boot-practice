package com.test.practice.comment.service;

import com.test.practice.comment.dto.CommentCreateRequest;
import com.test.practice.comment.dto.CommentResponse;
import com.test.practice.comment.dto.CommentUpdateRequest;
import com.test.practice.comment.entity.Comment;
import com.test.practice.comment.repository.CommentRepository;
import com.test.practice.post.entity.Post;
import com.test.practice.post.repository.PostRepository;
import com.test.practice.user.entity.Role;
import com.test.practice.user.entity.User;
import com.test.practice.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2024, 6, 1, 12, 0);

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Post post;
    private Post otherPost;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .username("u1")
                .password("ENC")
                .nickname("닉네임")
                .role(Role.USER)
                .build();

        post = Post.builder()
                .id(10L)
                .title("제목")
                .content("본문")
                .user(user)
                .viewCount(0L)
                .build();

        otherPost = Post.builder()
                .id(99L)
                .title("다른글")
                .content("다른본문")
                .user(user)
                .viewCount(0L)
                .build();
    }

    private Comment comment(long id, String content, Comment parent) {
        return Comment.builder()
                .id(id)
                .content(content)
                .user(user)
                .post(post)
                .parent(parent)
                .nickname(user.getNickname())
                .createTime(FIXED_TIME)
                .updateTime(FIXED_TIME)
                .deleteTime(null)
                .build();
    }

    // Create
    @Test
    @DisplayName("루트 댓글 작성 성공")
    void createComment_success_root() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("안녕하세요");
        request.setParentId(null);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            return comment(100L, c.getContent(), c.getParent());
        });

        CommentResponse response = commentService.createComment(10L, request);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getContent()).isEqualTo("안녕하세요");
        assertThat(response.getPostId()).isEqualTo(10L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getParentId()).isNull();
        assertThat(response.getDepth()).isZero();
        assertThat(response.getNickname()).isEqualTo("닉네임");

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("대댓글 작성 성공 — 부모가 루트일 때")
    void createComment_success_reply() {
        Comment root = comment(50L, "루트", null);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("대댓");
        request.setParentId(50L);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findByIdAndDeleteTimeIsNull(50L)).thenReturn(Optional.of(root));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment c = invocation.getArgument(0);
            return comment(51L, c.getContent(), c.getParent());
        });

        CommentResponse response = commentService.createComment(10L, request);

        assertThat(response.getId()).isEqualTo(51L);
        assertThat(response.getParentId()).isEqualTo(50L);
        assertThat(response.getDepth()).isEqualTo(1);

        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 — 게시글 없음")
    void createComment_fail_postNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("내용");

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 게시글입니다.");

        verify(userRepository, never()).findById(any());
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 — 사용자 없음")
    void createComment_fail_userNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(999L);
        request.setContent("내용");

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 사용자입니다.");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 — 부모 댓글 없음")
    void createComment_fail_parentNotFound() {
        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("내용");
        request.setParentId(50L);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findByIdAndDeleteTimeIsNull(50L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.createComment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 댓글입니다.");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 — 다른 게시글의 댓글에 답글")
    void createComment_fail_parentDifferentPost() {
        Comment rootOnOther = Comment.builder()
                .id(50L)
                .content("다른글댓글")
                .user(user)
                .post(otherPost)
                .parent(null)
                .nickname("닉네임")
                .createTime(FIXED_TIME)
                .updateTime(FIXED_TIME)
                .deleteTime(null)
                .build();

        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("답글");
        request.setParentId(50L);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findByIdAndDeleteTimeIsNull(50L)).thenReturn(Optional.of(rootOnOther));

        assertThatThrownBy(() -> commentService.createComment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("다른 게시글의 댓글에는 답글을 달 수 없습니다.");

        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 — 대댓글에 답글(3단 방지)")
    void createComment_fail_replyToReply() {
        Comment root = comment(50L, "루트", null);
        Comment reply = comment(51L, "대댓", root);

        CommentCreateRequest request = new CommentCreateRequest();
        request.setUserId(1L);
        request.setContent("금지");
        request.setParentId(51L);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(commentRepository.findByIdAndDeleteTimeIsNull(51L)).thenReturn(Optional.of(reply));

        assertThatThrownBy(() -> commentService.createComment(10L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("대댓글에는 답글을 달 수 없습니다. (최대 2단계)");

        verify(commentRepository, never()).save(any());
    }

    // Read
    @Test
    @DisplayName("댓글 트리 조회 성공 — 루트별 replies 묶임")
    void getCommentTree_success() {
        Comment root1 = comment(1L, "첫댓", null);
        Comment root2 = comment(3L, "둘째댓", null);
        Comment reply = comment(2L, "대댓", root1);

        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.of(post));
        when(commentRepository.findByPost_IdAndDeleteTimeIsNullOrderByCreateTimeAsc(10L))
                .thenReturn(List.of(root1, reply, root2));

        List<CommentResponse> tree = commentService.getCommentTree(10L);

        assertThat(tree).hasSize(2);
        assertThat(tree.get(0).getId()).isEqualTo(1L);
        assertThat(tree.get(0).getReplies()).hasSize(1);
        assertThat(tree.get(0).getReplies().get(0).getId()).isEqualTo(2L);
        assertThat(tree.get(0).getReplies().get(0).getDepth()).isEqualTo(1);

        assertThat(tree.get(1).getId()).isEqualTo(3L);
        assertThat(tree.get(1).getReplies()).isEmpty();
    }

    @Test
    @DisplayName("댓글 트리 조회 실패 — 게시글 없음")
    void getCommentTree_fail_postNotFound() {
        when(postRepository.findByIdAndDeleteTimeIsNull(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> commentService.getCommentTree(10L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 게시글입니다.");

        verify(commentRepository, never()).findByPost_IdAndDeleteTimeIsNullOrderByCreateTimeAsc(any());
    }

    // Update
    @Test
    @DisplayName("댓글 수정 성공")
    void updateComment_success() {
        Comment target = comment(5L, "이전", null);
        CommentUpdateRequest request = new CommentUpdateRequest();
        request.setContent("수정됨");

        when(commentRepository.findByIdAndDeleteTimeIsNull(5L)).thenReturn(Optional.of(target));

        CommentResponse response = commentService.updateComment(10L, 5L, 1L, request);

        assertThat(response.getContent()).isEqualTo("수정됨");
        assertThat(target.getContent()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("댓글 수정 실패 — 다른 게시글 경로")
    void updateComment_fail_wrongPostId() {
        Comment target = comment(5L, "내용", null);

        when(commentRepository.findByIdAndDeleteTimeIsNull(5L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> commentService.updateComment(999L, 5L, 1L, new CommentUpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 게시글의 댓글이 아닙니다.");
    }

    @Test
    @DisplayName("댓글 수정 실패 — 작성자 아님")
    void updateComment_fail_notAuthor() {
        Comment target = comment(5L, "내용", null);

        when(commentRepository.findByIdAndDeleteTimeIsNull(5L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> {
            CommentUpdateRequest req = new CommentUpdateRequest();
            req.setContent("해킹");
            commentService.updateComment(10L, 5L, 999L, req);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 작성자만 수정할 수 있습니다.");
    }

    // Delete
    @Test
    @DisplayName("댓글 삭제 성공 — 소프트 삭제")
    void deleteComment_success() {
        Comment target = comment(5L, "삭제대상", null);

        when(commentRepository.findByIdAndDeleteTimeIsNull(5L)).thenReturn(Optional.of(target));

        commentService.deleteComment(10L, 5L, 1L);

        assertThat(target.getDeleteTime()).isNotNull();
    }

    @Test
    @DisplayName("댓글 삭제 실패 — 작성자 아님")
    void deleteComment_fail_notAuthor() {
        Comment target = comment(5L, "내용", null);

        when(commentRepository.findByIdAndDeleteTimeIsNull(5L)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> commentService.deleteComment(10L, 5L, 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("댓글 작성자만 삭제할 수 있습니다.");

        assertThat(target.getDeleteTime()).isNull();
    }
}
