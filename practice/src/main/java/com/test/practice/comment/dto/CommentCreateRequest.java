package com.test.practice.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentCreateRequest {

    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long userId;

    /**
     * null 또는 생략: 게시글에 대한 루트 댓글.
     * 값 있음: 해당 댓글의 대댓글(부모는 반드시 루트여야 함 — 서비스에서 검증).
     */
    private Long parentId;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 300, message = "내용은 1자 이상 300자 이하로 입력해주세요.")
    private String content;
}
