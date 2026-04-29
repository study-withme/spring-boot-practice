package com.test.practice.post.dto;

import com.test.practice.post.entity.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostResponse {

    private Long id;
    private String title;
    private String content;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime deleteTime;

    private Long viewCount;

    private Long userId;
    private String username;
    private String nickname;

    public PostResponse(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();

        this.createTime = post.getCreateTime();
        this.updateTime = post.getUpdateTime();
        this.deleteTime = post.getDeleteTime();

        this.viewCount = post.getViewCount();

        this.userId = post.getUser().getId();
        this.username = post.getUser().getUsername();
        this.nickname = post.getUser().getNickname();
    }
}