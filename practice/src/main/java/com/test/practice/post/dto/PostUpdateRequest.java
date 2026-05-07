package com.test.practice.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 20, message = "제목은 1자 이상 20자 이하로 입력해주세요.")
    private String title;

    @NotBlank
    @Size(min = 1, max = 1500, message = "내용은 1자 이상 1500자 이하로 입력해주세요.")
    private String content;
}
