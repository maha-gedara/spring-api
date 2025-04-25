package com.skillverse.skillverse_backend.repository;

import com.skillverse.skillverse_backend.model.Post;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

//handle enviroment
public interface PostRepository extends MongoRepository<Post, String> {
    List<Post> findByUserId(String userId);
}