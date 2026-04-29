package com.test.practice.post.repository;

import com.test.practice.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    List<Post> findByDeleteTimeIsNull();

    Optional<Post> findByIdAndDeleteTimeIsNull(Long postId);

    List<Post> findByUser_IdAndDeleteTimeIsNull(Long userId);
}