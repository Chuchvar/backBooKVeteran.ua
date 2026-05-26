package com.example.sunatoriVeteran.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDTO {
    private Long id;
    private Long sanatoriumId;
    private Long userId;
    private String userName;
    private Integer rating;
    private String commentText;
    private LocalDateTime createdAt;
}
