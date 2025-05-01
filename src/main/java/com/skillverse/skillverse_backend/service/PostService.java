package com.skillverse.skillverse_backend.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.skillverse.skillverse_backend.model.Like;
import com.skillverse.skillverse_backend.model.Post;
import com.skillverse.skillverse_backend.repository.LikeRepository;
import com.skillverse.skillverse_backend.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final Storage storage;
    private final String bucketName = "skillverse-5f7bf.firebasestorage.app";

    public PostService(PostRepository postRepository, LikeRepository likeRepository, Storage storage) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.storage = storage;
    }

    public Post createPost(String userId, String title, String content, MultipartFile image, String imageUrl) throws IOException {
        logger.info("Creating post for user: {}, title: {}, content: {}, image: {}, imageUrl: {}",
                userId, title, content, image != null ? image.getOriginalFilename() : "null", imageUrl);
        Post post = new Post();
        post.setId(UUID.randomUUID().toString());
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setCreatedAt(LocalDateTime.now());
        post.setLikeCount(0);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            post.setImageUrl(imageUrl);
            logger.info("Using provided image URL: {}", imageUrl);
        }
        else if (image != null && !image.isEmpty()) {
            String fileName = post.getId() + "_" + image.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
            storage.create(blobInfo, image.getBytes());
            imageUrl = String.format("https://storage.googleapis.com/%s/posts/%s", bucketName, fileName);
            post.setImageUrl(imageUrl);
            logger.info("Uploaded image to Firebase, URL: {}", imageUrl);
        }

        Post savedPost = postRepository.save(post);
        logger.info("Post saved to MongoDB: {}", savedPost);
        return savedPost;
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(String id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    public List<Post> getPostsByUserId(String userId) {
        return postRepository.findByUserId(userId);
    }

    public Post updatePost(String id, String userId, String title, String content, MultipartFile image, String imageUrl) throws IOException {
        logger.info("Updating post id: {}, user: {}, title: {}, content: {}, image: {}, imageUrl: {}",
                id, userId, title, content, image != null ? image.getOriginalFilename() : "null", imageUrl);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own posts");
        }

        post.setTitle(title);
        post.setContent(content);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            if (post.getImageUrl() != null) {
                String oldFileName = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(bucketName, "posts/" + oldFileName);
                storage.delete(blobId);
                logger.info("Deleted old image: {}", oldFileName);
            }
            post.setImageUrl(imageUrl);
            logger.info("Using provided image URL: {}", imageUrl);
        } else if (image != null && !image.isEmpty()) {
            if (post.getImageUrl() != null) {
                String oldFileName = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(bucketName, "posts/" + oldFileName);
                storage.delete(blobId);
                logger.info("Deleted old image: {}", oldFileName);
            }

            String fileName = post.getId() + "_" + image.getOriginalFilename();
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
            storage.create(blobInfo, image.getBytes());
            imageUrl = String.format("https://storage.googleapis.com/%s/posts/%s", bucketName, fileName);
            post.setImageUrl(imageUrl);
            logger.info("Uploaded new image to Firebase, URL: {}", imageUrl);
        }

        Post savedPost = postRepository.save(post);
        logger.info("Post updated in MongoDB: {}", savedPost);
        return savedPost;
    }

    public void deletePost(String id, String userId) {
        logger.info("Deleting post id: {}, user: {}", id, userId);
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own posts");
        }

        if (post.getImageUrl() != null) {
            String fileName = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
            BlobId blobId = BlobId.of(bucketName, "posts/" + fileName);
            storage.delete(blobId);
            logger.info("Deleted image: {}", fileName);
        }

        postRepository.deleteById(id);
        logger.info("Post deleted from MongoDB: {}", id);
    }

    public String toggleLike(String postId, String userId) {
        logger.info("Toggling like for post: {}, user: {}", postId, userId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        Optional<Like> existingLike = likeRepository.findByPostIdAndUserId(postId, userId);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
            post.setLikeCount(post.getLikeCount() - 1);
            postRepository.save(post);
            logger.info("Unliked post: {}", postId);
            return "Post unliked successfully";
        } else {
            Like like = new Like();
            like.setId(UUID.randomUUID().toString());
            like.setPostId(postId);
            like.setUserId(userId);
            likeRepository.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
            postRepository.save(post);
            logger.info("Liked post: {}", postId);
            return "Post liked successfully";
        }
    }
}