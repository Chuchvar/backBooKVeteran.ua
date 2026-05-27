package com.example.sunatoriVeteran.service;

import com.example.sunatoriVeteran.model.Booking;
import com.example.sunatoriVeteran.model.BookingStatus;
import com.example.sunatoriVeteran.model.Sanatorium;
import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.repository.BookingRepository;
import com.example.sunatoriVeteran.repository.SanatoriumRepository;
import com.example.sunatoriVeteran.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SanatoriumRepository sanatoriumRepository;



    public Booking createBooking(String userEmail, Long sanatoriumId, 
                                  LocalDate checkInDate, LocalDate checkOutDate,
                                  String message, String packageType, String paymentMethod, Integer guestsCount) throws IOException {
        
        User user = userRepository.findFirstByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        // Всі користувачі можуть бронювати. Знижка буде застосована лише для верифікованих.

        Sanatorium sanatorium = sanatoriumRepository.findById(sanatoriumId)
                .orElseThrow(() -> new RuntimeException("Санаторій не знайдено"));

        if (sanatorium.getAvailableRooms() != null && sanatorium.getAvailableRooms() <= 0) {
            throw new RuntimeException("На жаль, у цьому санаторії більше немає вільних кімнат.");
        }

        Double pricePerDay = sanatorium.getStandardPackagePrice();
        if ("PREMIUM".equalsIgnoreCase(packageType)) {
            pricePerDay = sanatorium.getPremiumPackagePrice();
        } else if ("REHABILITATION".equalsIgnoreCase(packageType)) {
            pricePerDay = sanatorium.getRehabilitationPackagePrice();
        }
        if (pricePerDay == null) pricePerDay = 500.0;

        LocalDate today = LocalDate.now();
        if (checkInDate.isBefore(today)) {
            throw new RuntimeException("Дата заїзду не може бути в минулому");
        }
        if (checkInDate.isAfter(today.plusYears(1))) {
            throw new RuntimeException("Дата заїзду не може бути більше ніж на рік вперед");
        }

        long days = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days <= 0) {
            throw new RuntimeException("Дата виїзду має бути пізніше дати заїзду");
        }
        if (days > 60) {
            throw new RuntimeException("Максимальний термін бронювання - 60 днів");
        }
        
        Double basePrice = pricePerDay * days * guestsCount;
        Integer discount = 0;
        if (user.getVerificationStatus() == com.example.sunatoriVeteran.model.VerificationStatus.APPROVED) {
            discount = sanatorium.getDiscountPercentage() != null ? sanatorium.getDiscountPercentage() : 0;
        }
        
        Double totalPrice = basePrice * (1.0 - (discount / 100.0));
        if (totalPrice < 0) totalPrice = 0.0;

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setSanatorium(sanatorium);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setMessage(message);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setPackageType(packageType);
        booking.setPaymentMethod(paymentMethod);
        booking.setTotalPrice(totalPrice);
        booking.setGuestsCount(guestsCount);

        if (sanatorium.getAvailableRooms() != null) {
            sanatorium.setAvailableRooms(sanatorium.getAvailableRooms() - 1);
            sanatoriumRepository.save(sanatorium);
        }

        return bookingRepository.save(booking);
    }

    public List<Booking> getUserBookings(String userEmail) {
        User user = userRepository.findFirstByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public Booking updateBookingStatus(Long id, BookingStatus newStatus, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявку не знайдено"));
        booking.setStatus(newStatus);
        if (reason != null && !reason.trim().isEmpty()) {
            booking.setRejectionReason(reason);
        }
        return bookingRepository.save(booking);
    }

    public Resource getBookingDocument(Long id) throws Exception {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Заявку не знайдено"));
                
        String documentPath = booking.getDocumentPath();
        if (documentPath == null || documentPath.isEmpty()) {
            throw new RuntimeException("Документ не знайдено для цієї заявки");
        }
        
        Path path = Paths.get(documentPath);
        Resource resource = new UrlResource(path.toUri());
        
        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Не вдалося прочитати файл");
        }
    }

    public Booking payBooking(Long bookingId, String userEmail) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Заявку не знайдено"));

        if (!booking.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Це не ваше бронювання");
        }

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new RuntimeException("Оплатити можна тільки підтверджене бронювання");
        }

        booking.setStatus(BookingStatus.PAID);
        booking.setPaymentMethod("CARD");
        return bookingRepository.save(booking);
    }
}
