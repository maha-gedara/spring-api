package com.skillverse.skillverse_backend.service;

import com.skillverse.skillverse_backend.model.Comment;
import com.skillverse.skillverse_backend.repository.CommentRepository;
import com.skillverse.skillverse_backend.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CommentService {
//all the logics here
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public Comment createComment(String postId, String userId, String content) {
        // Verify post exists
        postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Comment comment = new Comment();
        comment.setId(UUID.randomUUID().toString());
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public List<Comment> getCommentsByPostId(String postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment getCommentById(String id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));
    }

    public Comment updateComment(String id, String userId, String content) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own comments");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    public String deleteComment(String id, String userId) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + id));

        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own comments");
        }

        commentRepository.deleteById(id);
        return "Comment successfully deleted";
    }
}