package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @PutMapping("/users/{id}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleUserBan(@PathVariable Long id, @RequestParam("banned") boolean banned) {
        return userRepository.findById(id).map(user -> {
            user.setBanned(banned);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("message", banned ? "Користувача заблоковано" : "Користувача розблоковано", "isBanned", user.isBanned()));
        }).orElse(ResponseEntity.notFound().build());
    }
}
