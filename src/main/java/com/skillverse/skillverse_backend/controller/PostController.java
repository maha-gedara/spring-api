package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.Post;
import com.skillverse.skillverse_backend.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PostController.class);
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPost(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Received POST /api/posts with title: {}, content: {}, image: {}, imageUrl: {}",
                title, content, image != null ? image.getOriginalFilename() : "null", imageUrl);
        try {
            Post post = postService.createPost(userId, title, content, image, imageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post created successfully");
            response.put("post", post);
            logger.info("Post created successfully: {}", post);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating post", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to create post: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
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
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Received PUT /api/posts/{} with title: {}, content: {}, image: {}, imageUrl: {}",
                id, title, content, image != null ? image.getOriginalFilename() : "null", imageUrl);
        try {
            Post updatedPost = postService.updatePost(id, userId, title, content, image, imageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Post updated successfully");
            response.put("post", updatedPost);
            logger.info("Post updated successfully: {}", updatedPost);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error updating post", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to update post: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePost(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Received DELETE /api/posts/{}", id);
        try {
            postService.deletePost(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Post successfully deleted");
            logger.info("Post deleted successfully: {}", id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error deleting post", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to delete post: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, String>> toggleLike(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Received POST /api/posts/{}/like for user: {}", id, userId);
        try {
            String message = postService.toggleLike(id, userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", message);
            logger.info("Like toggled successfully: {}", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error toggling like", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to toggle like: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}