package com.example.sunatoriVeteran.controller;

import com.example.sunatoriVeteran.model.Sanatorium;
import com.example.sunatoriVeteran.service.SanatoriumService;
import com.example.sunatoriVeteran.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
@RestController
@RequestMapping("/api/sanatoriums")
public class SanatoriumController {

    @Autowired
    private SanatoriumService service;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/top-rated")
    public List<Sanatorium> getTopRated(@RequestParam(defaultValue = "5") int limit) {
        return service.getTopRatedSanatoriums(limit);
    }

    @GetMapping("/regions")
    public List<String> getRegions() {
        return service.getDistinctRegions();
    }

    @GetMapping
    public Page<Sanatorium> getAll(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String profile,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sortOrder = direction.equalsIgnoreCase("desc")
                ? Sort.by(sort).descending()
                : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        return service.getFilteredSanatoriumsPaged(region, profile, specialization, search, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<Sanatorium> getById(@PathVariable Long id) {
        return service.getSanatoriumById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Sanatorium create(@RequestBody Sanatorium sanatorium) {
        Sanatorium saved = service.saveSanatorium(sanatorium);
        String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogService.logAction(adminEmail, "CREATE_SANATORIUM", saved.getId().toString(), "Created sanatorium: " + saved.getName());
        return saved;
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> uploadImages(@RequestParam("files") MultipartFile[] files) {
        if (files.length > 5) {
            return ResponseEntity.badRequest().build();
        }
        
        List<String> fileUrls = new ArrayList<>();
        String uploadDir = "uploads/";
        
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                
                String originalFilename = file.getOriginalFilename();
                String extension = (originalFilename != null && originalFilename.contains(".")) ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
                String fileName = UUID.randomUUID().toString() + extension;
                
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                
                String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(fileName)
                        .toUriString();
                
                fileUrls.add(fileDownloadUri);
            }
            return ResponseEntity.ok(fileUrls);
        } catch (IOException ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Sanatorium> update(@PathVariable Long id, @RequestBody Sanatorium sanatoriumDetails) {
        try {
            Sanatorium updatedSanatorium = service.updateSanatorium(id, sanatoriumDetails);
            String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.logAction(adminEmail, "UPDATE_SANATORIUM", id.toString(), "Updated sanatorium: " + updatedSanatorium.getName());
            return ResponseEntity.ok(updatedSanatorium);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.deleteSanatorium(id);
            String adminEmail = SecurityContextHolder.getContext().getAuthentication().getName();
            auditLogService.logAction(adminEmail, "DELETE_SANATORIUM", id.toString(), "Deleted sanatorium with ID: " + id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
