package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.model.Booking;
import com.example.sunatoriVeteran.service.BookingService;
import com.example.sunatoriVeteran.service.AuditLogService;
import com.example.sunatoriVeteran.security.JwtUtil;
import com.example.sunatoriVeteran.model.BookingStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @RequestHeader("Authorization") String token,
            @RequestParam("sanatoriumId") Long sanatoriumId,
            @RequestParam("checkInDate") String checkInDate,
            @RequestParam("checkOutDate") String checkOutDate,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "packageType", required = false, defaultValue = "STANDARD") String packageType,
            @RequestParam(value = "guestsCount", required = false, defaultValue = "1") Integer guestsCount) {
        
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            Booking booking = bookingService.createBooking(
                    email, sanatoriumId,
                    LocalDate.parse(checkInDate),
                    LocalDate.parse(checkOutDate),
                    message, packageType, "CARD", guestsCount);

            
            return ResponseEntity.ok(Map.of(
                    "message", "Заявку успішно створено",
                    "bookingId", booking.getId(),
                    "status", booking.getStatus().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/user/bookings")
    public ResponseEntity<?> getUserBookings(@RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            List<Booking> bookings = bookingService.getUserBookings(email);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllBookings() {
        try {
            List<Booking> bookings = bookingService.getAllBookings();
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/bookings/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookingStatus(
            @PathVariable Long id, 
            @RequestParam("status") String status,
            @RequestParam(value = "reason", required = false) String reason) {
        try {
            BookingStatus newStatus = BookingStatus.valueOf(status.toUpperCase());
            Booking updatedBooking = bookingService.updateBookingStatus(id, newStatus, reason);
            
            String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            String details = "Updated booking status to: " + newStatus;
            if (reason != null && !reason.isEmpty()) {
                details += " (Reason: " + reason + ")";
            }
            auditLogService.logAction(adminEmail, "UPDATE_BOOKING_STATUS", id.toString(), details);
            
            String expoToken = updatedBooking.getUser().getExpoPushToken();
            if (expoToken != null && !expoToken.isEmpty()) {
                try {
                    org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.set("Accept", "application/json");
                    
                    String sanatoriumName = updatedBooking.getSanatorium().getName();
                    String pushTitle = "Оновлення статусу заявки";
                    String pushBody = "Ваша заявка до санаторію " + sanatoriumName + " отримала статус: " + newStatus;
                    
                    String payload = String.format("{\"to\":\"%s\", \"title\":\"%s\", \"body\":\"%s\"}", expoToken, pushTitle, pushBody);
                    org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(payload, headers);
                    restTemplate.postForObject("https://exp.host/--/api/v2/push/send", entity, String.class);
                } catch (Exception e) {
                    System.out.println("Failed to send push notification: " + e.getMessage());
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Статус заявки оновлено",
                    "booking", updatedBooking
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Невірний статус"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/bookings/{id}/document")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingDocument(@PathVariable Long id) {
        try {
            Resource resource = bookingService.getBookingDocument(id);
            String contentType = "application/pdf";
            
            String filename = resource.getFilename();
            if (filename != null) {
                if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) contentType = "image/jpeg";
                else if (filename.toLowerCase().endsWith(".png")) contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/bookings/{id}/pay")
    public ResponseEntity<?> payBooking(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            Booking booking = bookingService.payBooking(id, email);
            return ResponseEntity.ok(Map.of(
                    "message", "Оплату успішно здійснено",
                    "booking", booking
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
