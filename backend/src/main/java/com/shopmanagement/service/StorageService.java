package com.shopmanagement.service;

import com.shopmanagement.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Storage service for handling file uploads
 * This implementation uses local file storage
 * Can be replaced with Firebase/Supabase implementation
 */
@Service
@Slf4j
public class StorageService {
    
    @Value("${storage.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${storage.base-url:http://localhost:8080/uploads}")
    private String baseUrl;
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif"};
    
    /**
     * Upload a file to storage
     * @param file The file to upload
     * @param folder The folder/directory to store the file
     * @return The URL of the uploaded file
     */
    public String uploadFile(MultipartFile file, String folder) {
        validateFile(file);
        
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, folder);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Return URL
            String fileUrl = baseUrl + "/" + folder + "/" + filename;
            log.info("File uploaded successfully: {}", fileUrl);
            return fileUrl;
            
        } catch (IOException e) {
            log.error("Failed to upload file", e);
            throw new ValidationException("Failed to upload file: " + e.getMessage());
        }
    }
    
    /**
     * Delete a file from storage
     * @param fileUrl The URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        try {
            // Extract file path from URL
            String filePath = fileUrl.replace(baseUrl + "/", "");
            Path path = Paths.get(uploadDir, filePath);
            
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("File deleted successfully: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", fileUrl, e);
            // Don't throw exception, just log the error
        }
    }
    
    /**
     * Validate uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size exceeds maximum limit of 5MB");
        }
        
        String contentType = file.getContentType();
        boolean isValidType = false;
        for (String allowedType : ALLOWED_IMAGE_TYPES) {
            if (allowedType.equals(contentType)) {
                isValidType = true;
                break;
            }
        }
        
        if (!isValidType) {
            throw new ValidationException("Invalid file type. Only JPEG, PNG, and GIF images are allowed");
        }
    }
}
