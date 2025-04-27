package com.skillverse.skillverse_backend.controller;

import com.skillverse.skillverse_backend.model.Marketplace;
import com.skillverse.skillverse_backend.service.MarketplaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/marketplace")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;

    public MarketplaceController(MarketplaceService marketplaceService) {
        this.marketplaceService = marketplaceService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createAd(
            @RequestParam("category") String category,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Marketplace ad = marketplaceService.createAd(userId, category, title, description, images);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ad created successfully");
        response.put("ad", ad);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<Marketplace>> getAllAds() {
        List<Marketplace> ads = marketplaceService.getAllAds();
        return ResponseEntity.ok(ads);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Marketplace> getAdById(@PathVariable String id) {
        Marketplace ad = marketplaceService.getAdById(id);
        return ResponseEntity.ok(ad);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Marketplace>> getAdsByUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Marketplace> ads = marketplaceService.getAdsByUserId(userId);
        return ResponseEntity.ok(ads);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateAd(
            @PathVariable String id,
            @RequestParam("category") String category,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Marketplace updatedAd = marketplaceService.updateAd(id, userId, category, title, description, images);
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Ad updated successfully");
        response.put("ad", updatedAd);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAd(@PathVariable String id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        String message = marketplaceService.deleteAd(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return ResponseEntity.ok(response);
    }
}