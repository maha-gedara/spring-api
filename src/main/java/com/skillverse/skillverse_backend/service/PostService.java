package com.skillverse.skillverse_backend.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.skillverse.skillverse_backend.model.Like;
import com.skillverse.skillverse_backend.model.Post;
import com.skillverse.skillverse_backend.repository.LikeRepository;
import com.skillverse.skillverse_backend.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//containing all the logics
@Service
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final Storage storage;
    private final String bucketName = "skillverse-5f7bf.appspot.com";

    public PostService(PostRepository postRepository, LikeRepository likeRepository, Storage storage) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.storage = storage;
    }

    public Post createPost(String userId, String title, String content, MultipartFile image) throws IOException {
        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikeCount(0); // Initialize like count

        if (image != null && !image.isEmpty()) {
            String fileName = post.getId() + "_" + image.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
            storage.create(blobInfo, image.getBytes());
            String imageUrl = String.format("https://storage.googleapis.com/%s/posts/%s", bucketName, fileName);
            post.setImageUrl(imageUrl);
        }

        return postRepository.save(post);
    }

    //Get all posts
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    //Get a single post by its ID
    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    //Get posts created by a specific user
    public List<Post> getPostsByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    //Update a post
    public Post updatePost(String id, String userId, String title, String content, MultipartFile image) throws IOException {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own posts");
        }

        post.setTitle(title);
        post.setContent(content);

        //Update new image
        if (image != null && !image.isEmpty()) {
            if (post.getImageUrl() != null) {
                String oldFileName = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(bucketName, "posts/" + oldFileName);
                storage.delete(blobId); //remove old image
            }

            //update new image
            String fileName = post.getId() + "_" + image.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
            storage.create(blobInfo, image.getBytes());
            String imageUrl = String.format("https://storage.googleapis.com/%s/posts/%s", bucketName, fileName);
            post.setImageUrl(imageUrl);
        }

        return postRepository.save(post);
    }

    //Delete a post
    public void deletePost(String id, String userId) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        //Only the owner can delete
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts");
        }

        //Delete image from cloud storage
        if (post.getImageUrl() != null) {
            String fileName = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            storage.delete(blobId);
        }

        postRepository.deleteById(id); //Delete from database
    }

    public String toggleLike(String postId, String userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            // User has already liked, so unlike
            likeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);  // Decrease like count
            postRepository.save(post);
            return "Post unliked successfully";
        } else {
            // User hasn't liked, so like
            Like like = new Like();
            like.setId(UUID.randomUUID().toString());
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1); // Increase like count
            postRepository.save(post);
            return "Post liked successfully";
        }
    }
}