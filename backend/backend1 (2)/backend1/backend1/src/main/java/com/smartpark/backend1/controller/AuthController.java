package com.smartpark.backend1.controller;

import com.smartpark.backend1.dto.AuthResponseDTO;
import com.smartpark.backend1.dto.ChangePasswordDTO;
import com.smartpark.backend1.dto.LoginRequestDTO;
import com.smartpark.backend1.dto.RegisterRequestDTO;
import com.smartpark.backend1.dto.UpdateProfileDTO;
import com.smartpark.backend1.dto.UserResponseDTO;
import com.smartpark.backend1.model.User;
import com.smartpark.backend1.repository.UserRepository;
import com.smartpark.backend1.security.JwtUtil;
import com.smartpark.backend1.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

  @Autowired private AuthService    authService;
  @Autowired private JwtUtil        jwtUtil;
  @Autowired private UserRepository userRepository;

  // ✅ POST /api/auth/register
  @PostMapping("/register")
  public ResponseEntity<?> register(
    @RequestBody RegisterRequestDTO dto) {
    try {
      return ResponseEntity.ok(authService.register(dto));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  // ✅ POST /api/auth/login
  @PostMapping("/login")
  public ResponseEntity<?> login(
    @RequestBody LoginRequestDTO dto) {
    try {
      return ResponseEntity.ok(authService.login(dto));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  // ✅ GET /api/auth/me
  @GetMapping("/me")
  public ResponseEntity<?> me(
    @RequestHeader("Authorization") String authHeader) {
    try {
      String token = authHeader.replace("Bearer ", "");
      String email = jwtUtil.extractEmail(token);
      User user = userRepository.findByEmail(email)
        .orElseThrow(() ->
          new RuntimeException("Utilisateur introuvable"));
      return ResponseEntity.ok(
        UserResponseDTO.builder()
          .id(user.getId())
          .nom(user.getNom())
          .email(user.getEmail())
          .telephone(user.getTelephone())
          .role(user.getRole())
          .actif(user.isActif())
          .build()
      );
    } catch (Exception e) {
      return ResponseEntity.status(401)
        .body(Map.of("erreur", "Token invalide"));
    }
  }

  // ✅ PUT /api/auth/profile
  @PutMapping("/profile")
  public ResponseEntity<?> updateProfile(
    @RequestHeader("Authorization") String authHeader,
    @RequestBody UpdateProfileDTO dto) {
    try {
      String token = authHeader.replace("Bearer ", "");
      String email = jwtUtil.extractEmail(token);
      AuthResponseDTO updated =
        authService.updateProfile(email, dto);
      return ResponseEntity.ok(updated);
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }

  // ✅ PUT /api/auth/change-password
  @PutMapping("/change-password")
  public ResponseEntity<?> changePassword(
    @RequestHeader("Authorization") String authHeader,
    @RequestBody ChangePasswordDTO dto) {
    try {
      String token = authHeader.replace("Bearer ", "");
      String email = jwtUtil.extractEmail(token);
      authService.changePassword(email, dto);
      return ResponseEntity.ok(
        Map.of("message",
          "Mot de passe modifié avec succès !"));
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest()
        .body(Map.of("erreur", e.getMessage()));
    }
  }
}
