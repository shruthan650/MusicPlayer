package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class SongService {
	
	final SongRepository repository;

	SongService(SongRepository repository) {
		this.repository = repository;
	}

	public List<Song> getAllSongs() {
		return repository.findAll();
	}
	
	public Song addSong(Song song) {
		return repository.save(song);
	}
	
	public Song getSongById(String id) {
		return repository.findById(id).orElse(null);
	}
	
	public void updateSongById(Song song, String id) {
		repository.save(song);
	}
	
	public void deleteSongById(String id) throws IOException {
		Song song = repository.findById(id).orElse(null);
		
		if (song != null) {
			if(song.getFilePath() != null) {
				Files.deleteIfExists(Paths.get(song.getFilePath()));
			}
			
			repository.deleteById(id);
		}
	}

	public void uploadSong(MultipartFile songFile, String songId) throws IOException {
		
		Song song = repository.findById(songId).orElse(null);
		
		String oldPath = song.getFilePath();
		
		if (songFile.isEmpty() ||
			songFile.getOriginalFilename().isEmpty() ||
			!songFile.getOriginalFilename().toLowerCase().contains(".wav") ||
			!"audio/wav".equals(songFile.getContentType())) {
			return;
		}
		
		byte[] songFileBytes = songFile.getBytes();
		
		Path folder = Paths.get("songs");
		String originalfileName = songFile.getOriginalFilename();
		String extension = originalfileName.substring(originalfileName.lastIndexOf("."));
		String fileName = UUID.randomUUID().toString() + extension;
		
		Path filePath = folder.resolve(fileName);
		
		Files.createDirectories(folder);
		Files.write(filePath, songFileBytes);
		
		song.setFilePath(filePath.toString());
		
		repository.save(song);
		
		if (oldPath != null) {
			Files.deleteIfExists(Paths.get(oldPath));
		}
	}

	public Resource streamSong(String songId) throws IOException {
		
		Song song = repository.findById(songId).orElse(null);
		
		Path filePath = Paths.get(song.getFilePath());
		
		return new FileSystemResource(filePath);
	}
	
}
