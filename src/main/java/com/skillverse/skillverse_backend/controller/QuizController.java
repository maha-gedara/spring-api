package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.Quiz;
import com.skillverse.skillverse_backend.service.QuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createQuiz(
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("time") int time,
            @RequestParam("link") String link) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Quiz quiz = quizService.createQuiz(userId, title, category, description, time, link);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Quiz created successfully");
        response.put("quiz", quiz);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        return ResponseEntity.ok(quizzes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable String id) {
        Quiz quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Quiz>> getQuizzesByUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Quiz> quizzes = quizService.getQuizzesByUserId(userId);
        return ResponseEntity.ok(quizzes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateQuiz(
            @PathVariable String id,
            @RequestParam("title") String title,
            @RequestParam("category") String category,
            @RequestParam("description") String description,
            @RequestParam("time") int time,
            @RequestParam("link") String link) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Quiz updatedQuiz = quizService.updateQuiz(id, userId, title, category, description, time, link);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Quiz updated successfully");
        response.put("quiz", updatedQuiz);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteQuiz(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String message = quizService.deleteQuiz(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}