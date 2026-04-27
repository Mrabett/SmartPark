// src/main/java/com/smartpark/evenements/controller/AiController.java
package com.smartpark.backend1.controller;

import com.smartpark.backend1.dto.AiGenerateRequest;
import com.smartpark.backend1.dto.AiGenerateResponse;
import com.smartpark.backend1.service.AiDescriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AiController {

  private final AiDescriptionService aiDescriptionService;

  /**
   * POST /api/ai/description
   * Génère une description d'événement via Claude (Anthropic)
   */
  @PostMapping("/description")
  public ResponseEntity<AiGenerateResponse> generate(
    @Valid @RequestBody AiGenerateRequest request) {

    String description = aiDescriptionService.generateDescription(request);
    return ResponseEntity.ok(
      new AiGenerateResponse(description, description.length())
    );
  }
}
