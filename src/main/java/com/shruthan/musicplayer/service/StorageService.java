package com.shruthan.musicplayer.service;

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(StorageService.class);

	private final Path songStorageLocation = Paths.get("storage/songs").toAbsolutePath().normalize();

	private final Path coverStorageLocation = Paths.get("storage/covers").toAbsolutePath().normalize();

	public StorageService() throws IOException {

		Files.createDirectories(songStorageLocation);
		Files.createDirectories(coverStorageLocation);

		logger.info("Storage directories initialized");
	}

	public String storeSongFile(MultipartFile file, Set<String> allowedExtensions) throws IOException {

		logger.debug("Storing song file");

		return storeFile(file, songStorageLocation, allowedExtensions);
	}

	public String storeCoverFile(MultipartFile file, Set<String> allowedExtensions) throws IOException {

		logger.debug("Storing cover image");

		return storeFile(file, coverStorageLocation, allowedExtensions);
	}

	private String storeFile(MultipartFile file, Path targetDir, Set<String> allowedExtensions) throws IOException {

		String originalFilename = file.getOriginalFilename();

		if (file.isEmpty() || originalFilename == null || originalFilename.isBlank()) {

			logger.warn("Invalid or empty file provided");

			throw new InvalidInputException("Invalid file provided");
		}

		String extension = getFileExtension(originalFilename).toLowerCase();

		if (!allowedExtensions.contains(extension)) {

			logger.warn("Unsupported file type attempted: .{}", extension);

			throw new InvalidInputException("Unsupported file type: ." + extension);
		}

		String fileName = UUID.randomUUID() + "." + extension;

		Path targetPath = targetDir.resolve(fileName).normalize();

		// Prevent path traversal outside target directory
		if (!targetPath.getParent().equals(targetDir)) {

			logger.warn("Path traversal attempt detected for file: {}", originalFilename);

			throw new InvalidInputException("Cannot store file outside current directory.");
		}

		Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

		logger.info("File stored successfully: {}", fileName);

		return targetPath.toString();
	}

	public Resource loadAsResource(String filePath) {

		if (filePath == null) {

			logger.warn("Attempted to load file with null path");

			throw new ResourceNotFoundException("File path is empty");
		}

		Path path = Paths.get(filePath).toAbsolutePath().normalize();

		Resource resource = new FileSystemResource(path);

		if (!resource.exists() || !resource.isReadable()) {

			logger.warn("File not found or not readable: {}", path);

			throw new ResourceNotFoundException("File not found or not readable");
		}

		logger.debug("File loaded successfully: {}", path);

		return resource;
	}

	public void deleteFileIfExists(String filePath) {

		if (filePath == null || filePath.isBlank()) {
			return;
		}

		try {

			Path path = Paths.get(filePath).toAbsolutePath().normalize();

			boolean deleted = Files.deleteIfExists(path);

			if (deleted) {
				logger.info("File deleted successfully: {}", path);
			} else {
				logger.debug("File did not exist: {}", path);
			}

		} catch (IOException e) {

			logger.warn("Failed to delete physical file: {}", filePath, e);
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