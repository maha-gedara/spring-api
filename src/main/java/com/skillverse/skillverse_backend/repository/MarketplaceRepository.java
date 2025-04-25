package com.skillverse.skillverse_backend.repository;

import com.skillverse.skillverse_backend.model.Marketplace;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MarketplaceRepository extends MongoRepository<Marketplace, String> {
    List<Marketplace> findByUserId(String userId);
}