package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.model.VerificationStatus;
import com.example.sunatoriVeteran.security.JwtUtil;
import com.example.sunatoriVeteran.service.VerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @Autowired
    private com.example.sunatoriVeteran.repository.UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/user/verify")
    public ResponseEntity<?> submitVerification(
            @RequestHeader("Authorization") String token,
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("document") MultipartFile document) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User updatedUser = verificationService.submitVerificationRequest(email, photo, document);
            return ResponseEntity.ok(Map.of(
                    "message", "Реквізити надіслані на перевірку",
                    "verificationStatus", updatedUser.getVerificationStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/verifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingVerifications() {
        try {
            List<User> pendingVerifications = verificationService.getPendingVerifications();
            return ResponseEntity.ok(pendingVerifications);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/admin/verifications/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVerificationStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "message", required = false) String message) {
        try {
            VerificationStatus newStatus = VerificationStatus.valueOf(status.toUpperCase());
            User updatedUser = verificationService.updateVerificationStatus(id, newStatus, message);
            return ResponseEntity.ok(Map.of(
                    "message", "Статус верифікації оновлено",
                    "status", updatedUser.getVerificationStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Невірний статус"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/verifications/{id}/file")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getVerificationFile(
            @PathVariable Long id,
            @RequestParam("type") String type) {
        try {
            Resource resource = verificationService.getFile(id, type);
            String contentType = "application/octet-stream";
            String filename = resource.getFilename();
            if (filename != null) {
                String lowerName = filename.toLowerCase();
                if (lowerName.endsWith(".pdf")) contentType = "application/pdf";
                else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg")) contentType = "image/jpeg";
                else if (lowerName.endsWith(".png")) contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/photo")
    public ResponseEntity<Resource> getMyPhoto(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
            
            Resource resource = verificationService.getFile(user.getId(), "photo");
            String contentType = "image/jpeg";
            String filename = resource.getFilename();
            if (filename != null && filename.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
