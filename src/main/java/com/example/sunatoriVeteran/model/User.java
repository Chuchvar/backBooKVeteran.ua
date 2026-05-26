package com.example.sunatoriVeteran.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String password;
    
    @Column(unique = true)
    private String phone;
    
    @Column(nullable = false)
    private boolean isBanned = false;
    
    @Column(nullable = false)
    private String role = "USER";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    private VerificationStatus verificationStatus = VerificationStatus.UNVERIFIED;

    @Column(columnDefinition = "varchar(500)")
    private String photoPath;

    @Column(columnDefinition = "varchar(500)")
    private String documentPath;

    @Column(columnDefinition = "text")
    private String verificationMessage;

    @Column(columnDefinition = "boolean default false")
    private boolean isEmailVerified = false;

    @Column(columnDefinition = "varchar(10)")
    private String emailVerificationCode;

    @Column(columnDefinition = "varchar(255)")
    private String expoPushToken;

    public User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isBanned() {
        return isBanned;
    }
    public void setBanned(boolean banned) {
        isBanned = banned;
    }
    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }
    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }
    public String getPhotoPath() {
        return photoPath;
    }
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
    }
    public String getDocumentPath() {
        return documentPath;
    }
    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
    public String getVerificationMessage() {
        return verificationMessage;
    }
    public void setVerificationMessage(String verificationMessage) {
        this.verificationMessage = verificationMessage;
    }
    public boolean isEmailVerified() {
        return isEmailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        isEmailVerified = emailVerified;
    }
    public String getEmailVerificationCode() {
        return emailVerificationCode;
    }
    public void setEmailVerificationCode(String emailVerificationCode) {
        this.emailVerificationCode = emailVerificationCode;
    }
    public String getExpoPushToken() {
        return expoPushToken;
    }
    public void setExpoPushToken(String expoPushToken) {
        this.expoPushToken = expoPushToken;
    }
}