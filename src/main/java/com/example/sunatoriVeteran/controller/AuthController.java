package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.sunatoriVeteran.security.JwtUtil;
import com.example.sunatoriVeteran.security.CustomUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.Random;

import com.example.sunatoriVeteran.service.EmailService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @jakarta.annotation.PostConstruct
    public void fixConstraints() {
        try {
            jdbcTemplate.execute("UPDATE users SET is_email_verified = true WHERE email = 'bogdan5.wap@gmail.com'");
            jdbcTemplate.execute(
                "DO $$ " +
                "DECLARE r record; " +
                "BEGIN " +
                "  FOR r IN ( " +
                "    SELECT tc.constraint_name " +
                "    FROM information_schema.table_constraints tc " +
                "    JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) " +
                "    WHERE constraint_type = 'UNIQUE' AND tc.table_name = 'users' AND ccu.column_name IN ('name', 'phone') " +
                "  ) LOOP " +
                "    EXECUTE 'ALTER TABLE users DROP CONSTRAINT ' || r.constraint_name; " +
                "  END LOOP; " +
                "END$$;"
            );
        } catch (Exception e) {
            System.out.println("Constraint fix skipped or failed");
        }
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Value("${google.client-id}")
    private String googleClientId;

    @Autowired
    private EmailService emailService;

    private String generateVerificationCode() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        if (userRepository.findFirstByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(400).body(Map.of("error", "Користувач з таким email вже існує"));
        }
        try {
            String rawPassword = user.getPassword();
            String encodedPassword = passwordEncoder.encode(rawPassword);
            user.setPassword(encodedPassword);
            
            String code = generateVerificationCode();
            user.setEmailVerificationCode(code);
            user.setEmailVerified(false);

            userRepository.save(user);

            emailService.sendVerificationCode(user.getEmail(), code);

            return ResponseEntity.ok(Map.of("message", "Success! User registered. Please check email for verification code."));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Користувач з таким іменем або номером телефону вже існує"));
        }
    }

    @PostMapping("/signup/complete")
    public ResponseEntity<?> signupComplete(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String code = payload.get("verification_code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email та код підтвердження обов'язкові"));
        }

        Optional<User> userOpt = userRepository.findFirstByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
        }

        User user = userOpt.get();
        if (user.isEmailVerified()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email вже підтверджено"));
        }

        if (code.equals(user.getEmailVerificationCode())) {
            user.setEmailVerified(true);
            user.setEmailVerificationCode(null);
            userRepository.save(user);

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(Map.of("message", "Email підтверджено", "token", jwt));
        } else {
            return ResponseEntity.status(400).body(Map.of("error", "Невірний код підтвердження"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginData) {
        Optional<User> userOpt = userRepository.findFirstByEmail(loginData.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.isBanned()) {
                return ResponseEntity.status(403).body(Map.of("error", "Ваш акаунт заблоковано адміністрацією"));
            }
            if (!user.isEmailVerified()) {
                return ResponseEntity.status(403).body(Map.of("error", "Будь ласка, підтвердіть свій email перед входом"));
            }
            boolean isMatch = passwordEncoder.matches(loginData.getPassword(), user.getPassword());

            if (isMatch) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String jwt = jwtUtil.generateToken(userDetails);
                return ResponseEntity.ok(Map.of("message", "Login Success", "token", jwt));
            } else {
                return ResponseEntity.status(401).body(Map.of("error", "Невірний пароль"));
            }
        }

        return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> body) {
        String idTokenString = body.get("credential");
        if (idTokenString == null || idTokenString.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Credential is missing"));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(java.util.Arrays.asList(googleClientId, "82683806631-78qjg3v982b0rori732v5uq9n7kc7gem.apps.googleusercontent.com"))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                try {
                    GoogleIdToken parsed = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), idTokenString);
                    if (parsed != null) {
                        return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token. Aud: " + parsed.getPayload().getAudience()));
                    }
                } catch (Exception parseEx) {
                    return ResponseEntity.status(401).body(Map.of("error", "Unparseable Google token."));
                }
                return ResponseEntity.status(401).body(Map.of("error", "Invalid Google token"));
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<User> existingUser = userRepository.findFirstByEmail(email);
            User user;
            if (existingUser.isPresent()) {
                user = existingUser.get();
            } else {
                user = new User();
                user.setEmail(email);
                user.setName(name != null ? name : email);
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                user.setRole("USER");
                user.setEmailVerified(true);
                userRepository.save(user);
            }

            if (user.isBanned()) {
                return ResponseEntity.status(403).body(Map.of("error", "Ваш акаунт заблоковано адміністрацією"));
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
            String jwt = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(Map.of("message", "Google Login Success", "token", jwt));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Акаунт з таким іменем або телефоном вже частково існує в базі."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Google auth failed: " + e.getMessage()));
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> getUserData(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Токен відсутній"));
        }
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            Optional<User> userOpt = userRepository.findFirstByEmail(email);

            if (userOpt.isPresent()) {
                return ResponseEntity.ok(Map.of("user", userOpt.get()));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Токен застарів"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Невірний токен"));
        }
    }

    @PutMapping("/user/push-token")
    public ResponseEntity<?> updatePushToken(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody Map<String, String> payload) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Токен відсутній"));
        }
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            Optional<User> userOpt = userRepository.findFirstByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String pushToken = payload.get("token");
                user.setExpoPushToken(pushToken);
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Push token updated successfully"));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Невірний токен"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody Map<String, String> payload) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Токен відсутній"));
        }
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            Optional<User> userOpt = userRepository.findFirstByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (payload.containsKey("name")) {
                    user.setName(payload.get("name"));
                }
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Профіль оновлено", "user", user));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Невірний токен"));
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody Map<String, String> payload) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Токен відсутній"));
        }
        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);
            Optional<User> userOpt = userRepository.findFirstByEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                String currentPassword = payload.get("currentPassword");
                String newPassword = payload.get("newPassword");

                if (currentPassword == null || newPassword == null) {
                     return ResponseEntity.badRequest().body(Map.of("error", "Необхідно вказати поточний та новий пароль"));
                }

                if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                    return ResponseEntity.status(400).body(Map.of("error", "Поточний пароль вказано невірно"));
                }

                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok(Map.of("message", "Пароль успішно змінено"));
            }
            return ResponseEntity.status(404).body(Map.of("error", "Користувача не знайдено"));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Невірний токен"));
        }
    }

}
