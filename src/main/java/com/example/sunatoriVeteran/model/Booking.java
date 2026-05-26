package com.example.sunatoriVeteran.model;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sanatorium_id", nullable = false)
    private Sanatorium sanatorium;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private BookingStatus status = BookingStatus.PENDING;

    @Column(columnDefinition = "varchar(500)")
    private String documentPath;

    @Column(columnDefinition = "text")
    private String message;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(columnDefinition = "numeric(10,2)")
    private Double totalPrice;

    @Column(columnDefinition = "varchar(50)")
    private String packageType;

    @Column(columnDefinition = "varchar(50)")
    private String paymentMethod;

    @Column(columnDefinition = "int default 1")
    private Integer guestsCount = 1;

    public Booking() {}
}
