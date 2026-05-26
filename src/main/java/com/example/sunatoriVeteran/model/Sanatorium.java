package com.example.sunatoriVeteran.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sanatoriums")
public class Sanatorium {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "varchar(255)")
    private String name;

    @Column(columnDefinition = "varchar(255)")
    private String region;

    @Column(columnDefinition = "varchar(255)")
    private String address;

    @Column(columnDefinition = "varchar(255)")
    private String medicalProfile;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @ElementCollection
    @CollectionTable(name = "sanatorium_images", joinColumns = @JoinColumn(name = "sanatorium_id"))
    @Column(name = "image_path")
    private java.util.List<String> imagePaths = new java.util.ArrayList<>();

    @ElementCollection(targetClass = SanatoriumSpecialization.class)
    @CollectionTable(name = "sanatorium_specializations", joinColumns = @JoinColumn(name = "sanatorium_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "specialization")
    private java.util.Set<SanatoriumSpecialization> specializations = new java.util.HashSet<>();

    @Column(columnDefinition = "numeric(3,1) default 0")
    private Double ratingBooking = 0.0;

    @Column(columnDefinition = "numeric(2,1) default 0")
    private Double ratingTripAdvisor = 0.0;

    @Transient
    private Double averageRating;

    @Transient
    private Integer reviewCount;

    @Column(columnDefinition = "numeric(10,2) default 500.0")
    private Double standardPackagePrice = 500.0;

    @Column(columnDefinition = "numeric(10,2) default 800.0")
    private Double premiumPackagePrice = 800.0;

    @Column(columnDefinition = "numeric(10,2) default 1200.0")
    private Double rehabilitationPackagePrice = 1200.0;

    @Column(columnDefinition = "integer default 10")
    private Integer availableRooms = 10;

    @Column(columnDefinition = "integer default 20")
    private Integer discountPercentage = 20;

    @Column(columnDefinition = "boolean default true")
    private Boolean hasStandardPackage = true;

    @Column(columnDefinition = "boolean default true")
    private Boolean hasPremiumPackage = true;

    @Column(columnDefinition = "boolean default true")
    private Boolean hasRehabilitationPackage = true;

    public Sanatorium() {
    }

    public Sanatorium(Long id, String name, String region, String address, String medicalProfile, String description, java.util.List<String> imagePaths) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.address = address;
        this.medicalProfile = medicalProfile;
        this.description = description;
        this.imagePaths = imagePaths;
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getMedicalProfile() { return medicalProfile; }
    public void setMedicalProfile(String medicalProfile) { this.medicalProfile = medicalProfile; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public java.util.List<String> getImagePaths() { return imagePaths; }
    public void setImagePaths(java.util.List<String> imagePaths) { this.imagePaths = imagePaths; }

    public java.util.Set<SanatoriumSpecialization> getSpecializations() { return specializations; }
    public void setSpecializations(java.util.Set<SanatoriumSpecialization> specializations) { this.specializations = specializations; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getReviewCount() { return reviewCount; }
    public void setReviewCount(Integer reviewCount) { this.reviewCount = reviewCount; }

    public Double getStandardPackagePrice() { return standardPackagePrice; }
    public void setStandardPackagePrice(Double standardPackagePrice) { this.standardPackagePrice = standardPackagePrice; }

    public Double getPremiumPackagePrice() { return premiumPackagePrice; }
    public void setPremiumPackagePrice(Double premiumPackagePrice) { this.premiumPackagePrice = premiumPackagePrice; }

    public Double getRehabilitationPackagePrice() { return rehabilitationPackagePrice; }
    public void setRehabilitationPackagePrice(Double rehabilitationPackagePrice) { this.rehabilitationPackagePrice = rehabilitationPackagePrice; }

    public Integer getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(Integer availableRooms) { this.availableRooms = availableRooms; }

    public Integer getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(Integer discountPercentage) { this.discountPercentage = discountPercentage; }

    public Boolean getHasStandardPackage() { return hasStandardPackage; }
    public void setHasStandardPackage(Boolean hasStandardPackage) { this.hasStandardPackage = hasStandardPackage; }

    public Boolean getHasPremiumPackage() { return hasPremiumPackage; }
    public void setHasPremiumPackage(Boolean hasPremiumPackage) { this.hasPremiumPackage = hasPremiumPackage; }

    public Boolean getHasRehabilitationPackage() { return hasRehabilitationPackage; }
    public void setHasRehabilitationPackage(Boolean hasRehabilitationPackage) { this.hasRehabilitationPackage = hasRehabilitationPackage; }

    public Double getRatingBooking() { return ratingBooking; }
    public void setRatingBooking(Double ratingBooking) { this.ratingBooking = ratingBooking; }

    public Double getRatingTripAdvisor() { return ratingTripAdvisor; }
    public void setRatingTripAdvisor(Double ratingTripAdvisor) { this.ratingTripAdvisor = ratingTripAdvisor; }
}