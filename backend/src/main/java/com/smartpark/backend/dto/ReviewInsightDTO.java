package com.smartpark.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReviewInsightDTO {
    private String produitId;
    private String resumeAdmin;
    private int noteConfiance;
    private long nbAvisAnalyses;
    private String dateGeneration;
}
