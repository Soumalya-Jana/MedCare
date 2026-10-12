package com.hospital.management.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.Objects;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads/lab-reports}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    public void init() {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize upload folder: " + uploadDir, e);
        }
    }

    /**
     * Stores an uploaded multipart file with a unique prefix and sanitized name.
     * Returns the relative stored filename.
     */
    public String storeFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        // Replace spaces and special characters with underscore for clean URLs
        String sanitizedFilename = rawOriginalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String uniqueFilename = (prefix != null ? prefix + "_" : "") + System.currentTimeMillis() + "_" + sanitizedFilename;

        try {
            Path targetLocation = this.uploadPath.resolve(uniqueFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return uniqueFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + sanitizedFilename + ". Please try again!", ex);
        }
    }

    /**
     * Loads a stored file as a Spring Resource for downloading or inline streaming.
     */
    public Resource loadFileAsResource(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return null;
        }
        try {
            Path filePath = this.uploadPath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                return null;
            }
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    /**
     * Deletes a stored file from disk if it exists.
     */
    public boolean deleteFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }
        try {
            Path filePath = this.uploadPath.resolve(filename).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Determines proper MIME Content-Type based on filename.
     */
    public String determineContentType(String filename) {
        if (filename == null) return "application/octet-stream";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".pdf")) return "application/pdf";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".txt")) return "text/plain";
        if (lower.endsWith(".dcm") || lower.endsWith(".dicom")) return "application/dicom";

        try {
            Path path = Paths.get(filename);
            String probed = Files.probeContentType(path);
            if (probed != null) return probed;
        } catch (Exception ignored) {}

        return "application/octet-stream";
    }

    public Path getUploadPath() {
        return uploadPath;
    }
}
