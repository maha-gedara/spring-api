package com.skillverse.skillverse_backend.service;

import com.skillverse.skillverse_backend.model.Quiz;
import com.skillverse.skillverse_backend.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class QuizService {

    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public Quiz createQuiz(String userId, String title, String category, String description, int time, String link) {
        Quiz quiz = new Quiz();
        quiz.setId(UUID.randomUUID().toString());
        quiz.setUserId(userId);
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setDescription(description);
        quiz.setTime(time);
        quiz.setLink(link);
        quiz.setCreatedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public Quiz getQuizById(String id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
    }

    public List<Quiz> getQuizzesByUserId(String userId) {
        return quizRepository.findByUserId(userId);
    }

    public Quiz updateQuiz(String id, String userId, String title, String category, String description, int time, String link) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));

        if (!quiz.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own quizzes");
        }

        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setDescription(description);
        quiz.setTime(time);
        quiz.setLink(link);

        return quizRepository.save(quiz);
    }

    public String deleteQuiz(String id, String userId) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));

        if (!quiz.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own quizzes");
        }

        quizRepository.deleteById(id);
        return "Quiz successfully deleted";
    }
}