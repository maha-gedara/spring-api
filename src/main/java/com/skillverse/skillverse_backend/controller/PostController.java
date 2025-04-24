package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.Post;
import com.skillverse.skillverse_backend.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Post post = postService.createPost(userId, title, content, image);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post created successfully");
        response.put("post", post);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable String id) {
        Post post = postService.getPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Post>> getPostsByUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Post> posts = postService.getPostsByUserId(userId);
        return ResponseEntity.ok(posts);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePost(
            @PathVariable String id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Post updatedPost = postService.updatePost(id, userId, title, content, image);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Post updated successfully");
        response.put("post", updatedPost);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        postService.deletePost(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Post successfully deleted");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, String>> toggleLike(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String message = postService.toggleLike(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}