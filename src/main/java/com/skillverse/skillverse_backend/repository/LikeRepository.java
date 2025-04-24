package com.skillverse.skillverse_backend.repository;

import com.skillverse.skillverse_backend.model.Like;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface LikeRepository extends MongoRepository<Like, String> {
    Optional<Like> findByPostIdAndUserId(String postId, String userId);
    long countByPostId(String postId);
}