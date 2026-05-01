package com.test.practice.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentUpdateRequest {

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 300, message = "내용은 1자 이상 300자 이하로 입력해주세요.")
    private String content;
}
