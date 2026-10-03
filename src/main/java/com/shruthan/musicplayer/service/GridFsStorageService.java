package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.io.InputStream;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;
import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;

@Service
public class GridFsStorageService {

	private static final Logger logger = LoggerFactory.getLogger(GridFsStorageService.class);

	private final GridFsTemplate gridFsTemplate;

	public GridFsStorageService(GridFsTemplate gridFsTemplate) {
		this.gridFsTemplate = gridFsTemplate;
	}

	public String store(MultipartFile file) throws IOException {

		if (file.isEmpty()) {
			logger.warn("Attempted to store an empty file");
			throw new InvalidInputException("File can't be empty");
		}

		String filename = file.getOriginalFilename();

		logger.info("Storing file in GridFS: {}", filename);

		try (InputStream inputStream = file.getInputStream()) {

			String fileId = gridFsTemplate.store(inputStream, filename, file.getContentType()).toString();

			logger.info("File stored successfully in GridFS: {} -> {}", filename, fileId);

			return fileId;

		} catch (IOException e) {

			logger.error("Failed to read file for GridFS storage: {}", filename, e);

			throw e;
		}
	}

	public GridFSFile findById(String fileId) {

		logger.debug("Searching GridFS file: {}", fileId);

		GridFSFile file = gridFsTemplate.findOne(getQuery(fileId));

		if (file == null) {

			logger.warn("GridFS file not found: {}", fileId);

			throw new ResourceNotFoundException("File not found");
		}

		logger.debug("GridFS file found: {} ({})", fileId, file.getFilename());

		return file;
	}

	public GridFsResource getResource(String fileId) {

		logger.debug("Loading GridFS resource: {}", fileId);

		return gridFsTemplate.getResource(findById(fileId));
	}

	public void delete(String fileId) {

		logger.info("Deleting GridFS file: {}", fileId);

		gridFsTemplate.delete(getQuery(fileId));

		logger.info("GridFS file deleted successfully: {}", fileId);
	}

	private Query getQuery(String fileId) {

		ObjectId objectId;

		try {

			objectId = new ObjectId(fileId);

		} catch (IllegalArgumentException e) {

			logger.warn("Invalid GridFS file ID: {}", fileId);

			throw new InvalidInputException("Invalid GridFS file ID");
		}

		return new Query(Criteria.where("_id").is(objectId));
	}
}