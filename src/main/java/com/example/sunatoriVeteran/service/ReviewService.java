package com.example.sunatoriVeteran.service;

import com.example.sunatoriVeteran.dto.ReviewCreateDTO;
import com.example.sunatoriVeteran.dto.ReviewDTO;
import com.example.sunatoriVeteran.model.Review;
import com.example.sunatoriVeteran.model.Sanatorium;
import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.repository.ReviewRepository;
import com.example.sunatoriVeteran.repository.SanatoriumRepository;
import com.example.sunatoriVeteran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private SanatoriumRepository sanatoriumRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ReviewDTO> getReviewsForSanatorium(Long sanatoriumId) {
        return reviewRepository.findBySanatoriumIdOrderByCreatedAtDesc(sanatoriumId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ReviewDTO addReview(Long sanatoriumId, ReviewCreateDTO createDTO, String userEmail) {
        Sanatorium sanatorium = sanatoriumRepository.findById(sanatoriumId)
                .orElseThrow(() -> new RuntimeException("Sanatorium not found"));

        User user = null;
        if (userEmail != null) {
            user = userRepository.findFirstByEmail(userEmail).orElse(null);
        }

        Review review = new Review();
        review.setSanatorium(sanatorium);
        review.setUser(user);
        review.setRating(createDTO.getRating());
        review.setCommentText(createDTO.getCommentText());
        review.setCreatedAt(LocalDateTime.now());

        review = reviewRepository.save(review);
        return mapToDTO(review);
    }

    private ReviewDTO mapToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setSanatoriumId(review.getSanatorium().getId());
        dto.setRating(review.getRating());
        dto.setCommentText(review.getCommentText());
        dto.setCreatedAt(review.getCreatedAt());

        if (review.getUser() != null) {
            dto.setUserId(review.getUser().getId());
            dto.setUserName(review.getUser().getName());
        } else {
            dto.setUserName("Анонім користувач");
        }

        return dto;
    }
}
