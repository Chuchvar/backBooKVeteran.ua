package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.model.SupportChat;
import com.example.sunatoriVeteran.model.SupportMessage;
import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.repository.SupportChatRepository;
import com.example.sunatoriVeteran.repository.SupportMessageRepository;
import com.example.sunatoriVeteran.repository.UserRepository;
import com.example.sunatoriVeteran.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/support")
public class SupportController {

    @Autowired
    private SupportChatRepository chatRepository;

    @Autowired
    private SupportMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/chats")
    public ResponseEntity<?> createChat(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> body) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            SupportChat chat = new SupportChat();
            chat.setUserId(user.getId());
            chat.setUserName(user.getName() != null ? user.getName() : user.getEmail());
            chat.setSubject(body.getOrDefault("subject", "Загальне питання"));
            chat.setStatus("OPEN");
            chat.setCreatedAt(LocalDateTime.now());
            chatRepository.save(chat);

            String firstMessage = body.get("message");
            if (firstMessage != null && !firstMessage.isBlank()) {
                SupportMessage msg = new SupportMessage();
                msg.setChatId(chat.getId());
                msg.setSenderId(user.getId());
                msg.setSenderRole("USER");
                msg.setSenderName(user.getName() != null ? user.getName() : user.getEmail());
                msg.setMessage(firstMessage);
                msg.setCreatedAt(LocalDateTime.now());
                messageRepository.save(msg);
            }

            return ResponseEntity.ok(chat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/chats/my")
    public ResponseEntity<?> getMyChats(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            List<SupportChat> chats = chatRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
            return ResponseEntity.ok(chats);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/chats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllChats(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            List<SupportChat> unassignedChats = chatRepository.findByAdminIdIsNullOrderByCreatedAtDesc();
            List<SupportChat> myChats = chatRepository.findByAdminIdOrderByCreatedAtDesc(user.getId());
            
            java.util.List<SupportChat> combined = new java.util.ArrayList<>();
            combined.addAll(unassignedChats);
            combined.addAll(myChats);
            combined.sort((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()));

            return ResponseEntity.ok(combined);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/chats/{chatId}/take")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> takeChat(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatId) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            SupportChat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Чат не знайдено"));

            if (chat.getAdminId() != null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Цей чат вже взято іншим адміністратором"));
            }

            chat.setAdminId(user.getId());
            chatRepository.save(chat);

            return ResponseEntity.ok(chat);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/chats/{chatId}/messages")
    public ResponseEntity<?> getChatMessages(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatId) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            SupportChat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Чат не знайдено"));

            if (!chat.getUserId().equals(user.getId())) {
                if (!"ADMIN".equals(user.getRole())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Доступ заборонено"));
                } else if (chat.getAdminId() != null && !chat.getAdminId().equals(user.getId())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Доступ заборонено. Чат взято іншим адміністратором."));
                }
            }

            List<SupportMessage> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/chats/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @RequestHeader("Authorization") String token,
            @PathVariable Long chatId,
            @RequestBody Map<String, String> body) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userRepository.findFirstByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

            SupportChat chat = chatRepository.findById(chatId)
                    .orElseThrow(() -> new RuntimeException("Чат не знайдено"));

            if (!chat.getUserId().equals(user.getId())) {
                if (!"ADMIN".equals(user.getRole())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Доступ заборонено"));
                } else if (chat.getAdminId() != null && !chat.getAdminId().equals(user.getId())) {
                    return ResponseEntity.status(403).body(Map.of("error", "Доступ заборонено. Чат взято іншим адміністратором."));
                } else if (chat.getAdminId() == null) {
                    return ResponseEntity.status(403).body(Map.of("error", "Спочатку візьміть цей чат, щоб відповісти."));
                }
            }

            if ("CLOSED".equals(chat.getStatus())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Чат закрито"));
            }

            SupportMessage msg = new SupportMessage();
            msg.setChatId(chatId);
            msg.setSenderId(user.getId());
            msg.setSenderRole(user.getRole());
            msg.setSenderName(user.getName() != null ? user.getName() : user.getEmail());
            msg.setMessage(body.get("message"));
            msg.setCreatedAt(LocalDateTime.now());
            messageRepository.save(msg);

            return ResponseEntity.ok(msg);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/chats/{chatId}/close")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> closeChat(@PathVariable Long chatId) {
        return chatRepository.findById(chatId).map(chat -> {
            chat.setStatus("CLOSED");
            chat.setClosedAt(LocalDateTime.now());
            chatRepository.save(chat);
            return ResponseEntity.ok(Map.of("message", "Чат закрито"));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/chats/{chatId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteChat(@PathVariable Long chatId) {
        try {
            List<SupportMessage> messages = messageRepository.findByChatIdOrderByCreatedAtAsc(chatId);
            messageRepository.deleteAll(messages);
            
            chatRepository.deleteById(chatId);
            
            return ResponseEntity.ok(Map.of("message", "Чат успішно видалено"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Помилка при видаленні чату: " + e.getMessage()));
        }
    }
}
