package com.shruthan.musicplayer.service;

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class StorageService {

    private final Path songStorageLocation = Paths.get("storage/songs").toAbsolutePath().normalize();
    private final Path coverStorageLocation = Paths.get("storage/covers").toAbsolutePath().normalize();

    public StorageService() throws IOException {
        Files.createDirectories(songStorageLocation);
        Files.createDirectories(coverStorageLocation);
    }

    public String storeSongFile(MultipartFile file, Set<String> allowedExtensions) throws IOException {
        return storeFile(file, songStorageLocation, allowedExtensions);
    }

    public String storeCoverFile(MultipartFile file, Set<String> allowedExtensions) throws IOException {
        return storeFile(file, coverStorageLocation, allowedExtensions);
    }

    private String storeFile(MultipartFile file, Path targetDir, Set<String> allowedExtensions) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (file.isEmpty() || originalFilename == null || originalFilename.isBlank()) {
            throw new InvalidInputException("Invalid file provided");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new InvalidInputException("Unsupported file type: ." + extension);
        }

        String fileName = UUID.randomUUID() + "." + extension;
        Path targetPath = targetDir.resolve(fileName).normalize();

        // Prevent path traversal outside target directory
        if (!targetPath.getParent().equals(targetDir)) {
            throw new InvalidInputException("Cannot store file outside current directory.");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath.toString();
    }

    public Resource loadAsResource(String filePath) {
        if (filePath == null) {
            throw new ResourceNotFoundException("File path is empty");
        }

        Path path = Paths.get(filePath).toAbsolutePath().normalize();
        Resource resource = new FileSystemResource(path);

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("File not found or not readable");
        }

        return resource;
    }

    public void deleteFileIfExists(String filePath) {
        if (filePath == null || filePath.isBlank())
            return;
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log warning: Failed to delete physical file
        }
    }

    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }
}
