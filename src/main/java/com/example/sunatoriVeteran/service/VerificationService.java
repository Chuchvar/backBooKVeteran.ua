package com.example.sunatoriVeteran.service;

import com.example.sunatoriVeteran.model.User;
import com.example.sunatoriVeteran.model.VerificationStatus;
import com.example.sunatoriVeteran.repository.UserRepository;
import com.example.sunatoriVeteran.util.FileEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VerificationService {

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR_PHOTOS = "uploads/verifications/photos/";
    private static final String UPLOAD_DIR_DOCS = "uploads/verifications/documents/";

    public User submitVerificationRequest(String email, MultipartFile photo, MultipartFile document) throws Exception {
        User user = userRepository.findFirstByEmail(email)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("Фото є обов'язковим");
        }
        if (document == null || document.isEmpty()) {
            throw new RuntimeException("Документ є обов'язковим");
        }

        Path photoDir = Paths.get(UPLOAD_DIR_PHOTOS);
        if (!Files.exists(photoDir)) Files.createDirectories(photoDir);
        String photoName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path photoPath = photoDir.resolve(photoName);
        byte[] encryptedPhoto = FileEncryptionUtil.encrypt(photo.getBytes());
        Files.write(photoPath, encryptedPhoto);

        Path docDir = Paths.get(UPLOAD_DIR_DOCS);
        if (!Files.exists(docDir)) Files.createDirectories(docDir);
        String docName = UUID.randomUUID() + "_" + document.getOriginalFilename();
        Path docPath = docDir.resolve(docName);
        byte[] encryptedDoc = FileEncryptionUtil.encrypt(document.getBytes());
        Files.write(docPath, encryptedDoc);

        user.setPhotoPath(photoPath.toString());
        user.setDocumentPath(docPath.toString());
        user.setVerificationStatus(VerificationStatus.PENDING);
        user.setVerificationMessage(null);

        return userRepository.save(user);
    }

    public List<User> getPendingVerifications() {
        return userRepository.findAll().stream()
                .filter(u -> u.getVerificationStatus() == VerificationStatus.PENDING)
                .collect(Collectors.toList());
    }

    public User updateVerificationStatus(Long userId, VerificationStatus newStatus, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));
        
        user.setVerificationStatus(newStatus);
        if (message != null && !message.trim().isEmpty()) {
            user.setVerificationMessage(message);
        }
        return userRepository.save(user);
    }

    public Resource getFile(Long userId, String type) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        String filePathString = type.equals("photo") ? user.getPhotoPath() : user.getDocumentPath();
        if (filePathString == null || filePathString.isEmpty()) {
            throw new RuntimeException("Файл не знайдено");
        }

        Path path = Paths.get(filePathString);
        if (Files.exists(path)) {
            try {
                byte[] encryptedData = Files.readAllBytes(path);
                byte[] decryptedData = FileEncryptionUtil.decrypt(encryptedData);
                return new ByteArrayResource(decryptedData) {
                    @Override
                    public String getFilename() {
                        return path.getFileName().toString();
                    }
                };
            } catch (Exception e) {
                return new org.springframework.core.io.UrlResource(path.toUri());
            }
        } else {
            throw new RuntimeException("Файл не знайдено на диску");
        }
    }
}
