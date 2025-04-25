package com.skillverse.skillverse_backend.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.skillverse.skillverse_backend.model.Marketplace;
import com.skillverse.skillverse_backend.repository.MarketplaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MarketplaceService {

    private final MarketplaceRepository marketplaceRepository;
    private final Storage storage;
    private final String bucketName = "skillverse-5f7bf.appspot.com";

    public MarketplaceService(MarketplaceRepository marketplaceRepository, Storage storage) {
        this.marketplaceRepository = marketplaceRepository;
        this.storage = storage;
    }

    public Marketplace createAd(String userId, String category, String title, String description, List<MultipartFile> images) throws IOException {
        if (images != null && images.size() > 3) {
            throw new RuntimeException("Cannot upload more than 3 images");
        }

        Marketplace ad = new Marketplace();
        ad.setId(UUID.randomUUID().toString());
        ad.setUserId(userId);
        ad.setCategory(category);
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setCreatedAt(LocalDateTime.now());

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = ad.getId() + "_" + UUID.randomUUID() + "_" + image.getOriginalFilename();
                    BlobId blobId = BlobId.of(bucketName, "marketplace/" + fileName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
                    storage.create(blobInfo, image.getBytes());
                    String imageUrl = String.format("https://storage.googleapis.com/%s/marketplace/%s", bucketName, fileName);
                    imageUrls.add(imageUrl);
                }
            }
            ad.setImageUrls(imageUrls);
        }

        return marketplaceRepository.save(ad);
    }

    public List<Marketplace> getAllAds() {
        return marketplaceRepository.findAll();
    }

    public Marketplace getAdById(String id) {
        return marketplaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));
    }

    public List<Marketplace> getAdsByUserId(String userId) {
        return marketplaceRepository.findByUserId(userId);
    }

    public Marketplace updateAd(String id, String userId, String category, String title, String description, List<MultipartFile> images) throws IOException {
        Marketplace ad = marketplaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));

        if (!ad.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only update your own ads");
        }

        if (images != null && images.size() > 3) {
            throw new RuntimeException("Cannot upload more than 3 images");
        }

        ad.setCategory(category);
        ad.setTitle(title);
        ad.setDescription(description);

        // Delete old images if new ones are provided
        if (images != null && !images.isEmpty()) {
            if (ad.getImageUrls() != null) {
                for (String imageUrl : ad.getImageUrls()) {
                    String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                    BlobId blobId = BlobId.of(bucketName, "marketplace/" + fileName);
                    storage.delete(blobId);
                }
            }

            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    String fileName = ad.getId() + "_" + UUID.randomUUID() + "_" + image.getOriginalFilename();
                    BlobId blobId = BlobId.of(bucketName, "marketplace/" + fileName);
                    BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(image.getContentType()).build();
                    storage.create(blobInfo, image.getBytes());
                    String imageUrl = String.format("https://storage.googleapis.com/%s/marketplace/%s", bucketName, fileName);
                    imageUrls.add(imageUrl);
                }
            }
            ad.setImageUrls(imageUrls);
        }

        return marketplaceRepository.save(ad);
    }

    public String deleteAd(String id, String userId) {
        Marketplace ad = marketplaceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found with id: " + id));

        if (!ad.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own ads");
        }

        // Delete images from Firebase
        if (ad.getImageUrls() != null) {
            for (String imageUrl : ad.getImageUrls()) {
                String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                BlobId blobId = BlobId.of(bucketName, "marketplace/" + fileName);
                storage.delete(blobId);
            }
        }

        marketplaceRepository.deleteById(id);
        return "Ad successfully deleted";
    }
}