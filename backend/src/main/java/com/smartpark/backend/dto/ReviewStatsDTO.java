package com.smartpark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class ReviewStatsDTO {
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingDistribution;
}
