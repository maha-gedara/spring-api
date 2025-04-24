package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.Comment;
import com.skillverse.skillverse_backend.service.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createComment(
            @RequestParam("postId") String postId,
            @RequestParam("content") String content) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment comment = commentService.createComment(postId, userId, content);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comment created successfully");
        response.put("comment", comment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Comment>> getCommentsByPostId(@PathVariable String postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getCommentById(@PathVariable String id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateComment(
            @PathVariable String id,
            @RequestParam("content") String content) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Comment updatedComment = commentService.updateComment(id, userId, content);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Comment updated successfully");
        response.put("comment", updatedComment);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteComment(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String message = commentService.deleteComment(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}