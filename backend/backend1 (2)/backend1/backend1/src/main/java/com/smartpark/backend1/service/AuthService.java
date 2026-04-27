package com.smartpark.backend1.service;

import com.smartpark.backend1.dto.AuthResponseDTO;
import com.smartpark.backend1.dto.ChangePasswordDTO;
import com.smartpark.backend1.dto.LoginRequestDTO;
import com.smartpark.backend1.dto.RegisterRequestDTO;
import com.smartpark.backend1.dto.UpdateProfileDTO;
import com.smartpark.backend1.dto.UserResponseDTO;
import com.smartpark.backend1.model.User;
import com.smartpark.backend1.repository.UserRepository;
import com.smartpark.backend1.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

  @Autowired private UserRepository  userRepository;
  @Autowired private JwtUtil         jwtUtil;
  @Autowired private PasswordEncoder passwordEncoder;

  // ✅ Inscription
  public AuthResponseDTO register(RegisterRequestDTO dto) {
    if (userRepository.existsByEmail(dto.getEmail())) {
      throw new RuntimeException("Email déjà utilisé !");
    }
    User user = new User();
    user.setNom(dto.getNom());
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setTelephone(dto.getTelephone());
    user.setRole("USER");
    user.setActif(true);
    userRepository.save(user);
    String token = jwtUtil.generateToken(
      user.getEmail(), user.getRole());
    return buildAuthResponse(user, token, "Inscription réussie !");
  }

  // ✅ Connexion
  public AuthResponseDTO login(LoginRequestDTO dto) {
    User user = userRepository.findByEmail(dto.getEmail())
      .orElseThrow(() -> new RuntimeException(
        "Email ou mot de passe incorrect"));
    if (!user.isActif()) {
      throw new RuntimeException("Compte désactivé");
    }
    if (!passwordEncoder.matches(
      dto.getPassword(), user.getPassword())) {
      throw new RuntimeException(
        "Email ou mot de passe incorrect");
    }
    String token = jwtUtil.generateToken(
      user.getEmail(), user.getRole());
    return buildAuthResponse(user, token, "Connexion réussie !");
  }

  // ✅ Modifier profil
  public AuthResponseDTO updateProfile(
    String email, UpdateProfileDTO dto) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() ->
        new RuntimeException("Utilisateur introuvable"));
    if (dto.getNom() != null && !dto.getNom().isBlank()) {
      user.setNom(dto.getNom());
    }
    if (dto.getTelephone() != null
      && !dto.getTelephone().isBlank()) {
      user.setTelephone(dto.getTelephone());
    }
    userRepository.save(user);
    String newToken = jwtUtil.generateToken(
      user.getEmail(), user.getRole());
    return buildAuthResponse(user, newToken, "Profil mis à jour !");
  }

  // ✅ Changer mot de passe
  public void changePassword(
    String email, ChangePasswordDTO dto) {
    User user = userRepository.findByEmail(email)
      .orElseThrow(() ->
        new RuntimeException("Utilisateur introuvable"));
    if (!passwordEncoder.matches(
      dto.getAncienPassword(), user.getPassword())) {
      throw new RuntimeException(
        "Ancien mot de passe incorrect !");
    }
    if (dto.getNouveauPassword() == null
      || dto.getNouveauPassword().length() < 6) {
      throw new RuntimeException(
        "Le nouveau mot de passe doit avoir au moins 6 caractères !");
    }
    user.setPassword(
      passwordEncoder.encode(dto.getNouveauPassword()));
    userRepository.save(user);
  }

  // ✅ Admin par défaut
  public void createDefaultAdmin() {
    if (!userRepository.existsByEmail("admin@smartpark.tn")) {
      User admin = new User();
      admin.setNom("Administrateur");
      admin.setEmail("admin@smartpark.tn");
      admin.setPassword(passwordEncoder.encode("admin123"));
      admin.setRole("ADMIN");
      admin.setActif(true);
      userRepository.save(admin);
      System.out.println(
        "✅ Admin créé : admin@smartpark.tn / admin123");
    }
  }

  // ✅ Builder
  private AuthResponseDTO buildAuthResponse(
    User user, String token, String message) {
    return AuthResponseDTO.builder()
      .token(token)
      .email(user.getEmail())
      .nom(user.getNom())
      .role(user.getRole())
      .telephone(user.getTelephone())
      .message(message)
      .build();
  }
}
