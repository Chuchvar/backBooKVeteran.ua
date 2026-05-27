package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.dto.ReviewCreateDTO;
import com.example.sunatoriVeteran.dto.ReviewDTO;
import com.example.sunatoriVeteran.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sanatoriums/{sanatoriumId}/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewDTO>> getReviews(@PathVariable Long sanatoriumId) {
        return ResponseEntity.ok(reviewService.getReviewsForSanatorium(sanatoriumId));
    }

    @PostMapping
    public ResponseEntity<?> addReview(
            @PathVariable Long sanatoriumId,
            @RequestBody ReviewCreateDTO reviewDTO,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Необхідно увійти в акаунт для написання відгуку"));
        }
        String email = authentication.getName();
        ReviewDTO created = reviewService.addReview(sanatoriumId, reviewDTO, email);
        return ResponseEntity.ok(created);
    }
}
