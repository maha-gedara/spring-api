package com.skillverse.skillverse_backend.repository;

import com.skillverse.skillverse_backend.model.Quiz;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface QuizRepository extends MongoRepository<Quiz, String> {
    List<Quiz> findByUserId(String userId);
}