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

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;

@Service
public class SongService {

	final SongRepository songRepository;
	final PlaylistRepository playlistRepository;
	final SecurityService securityService;

	SongService(SongRepository repository,
			PlaylistRepository playlistRepository,
			UserRepository userRepository,
			SecurityService securityService) {
		
		this.songRepository = repository;
		this.playlistRepository = playlistRepository;
		this.securityService = securityService;
	}

	public List<Song> getAllSongs() {
		return songRepository.findAll();
	}

	public Song addSong(Song song) {
		
		User user = securityService.getCurrentUser();
		song.setOwnerId(user.getId());
		
		return songRepository.save(song);
	}

	public Song getSongById(String songId) {
		Song song = songRepository.findById(songId).orElse(null);
		
		if (song == null) {
			throw new ResourceNotFoundException(songId + " is not present");
		}
		
		return song;
	}

	public void updateSongById(Song song, String songId) {

		Song songInRepo = songRepository.findById(songId).orElse(null);

		if (songInRepo != null) {

			songInRepo.setAlbumName(song.getAlbumName());
			songInRepo.setArtistName(song.getArtistName());
			songInRepo.setDuration(song.getDuration());
			songInRepo.setGenre(song.getGenre());
			songInRepo.setTitle(song.getTitle());
			songRepository.save(song);

		} else {
			throw new ResourceNotFoundException("Song not found");
		}
	}

	public void deleteSongById(String songId) throws IOException {
		Song song = songRepository.findById(songId).orElse(null);

		if (song != null) {
			if (song.getFilePath() != null) {
				Files.deleteIfExists(Paths.get(song.getFilePath()));
			}

			songRepository.deleteById(songId);

			List<Playlist> playlistsContainingSongId = playlistRepository.findBySongIdsContaining(songId);

			for (Playlist playlist : playlistsContainingSongId) {
				playlist.getSongIds().remove(songId);
				playlistRepository.save(playlist);
			}
		} else {
			throw new ResourceNotFoundException("Song not found");
		}
	}

	public void uploadSong(MultipartFile songFile, String songId) throws IOException {

		Song song = songRepository.findById(songId).orElse(null);

		String oldPath = song.getFilePath();

		if (songFile.isEmpty() || songFile.getOriginalFilename().isEmpty()
				|| !songFile.getOriginalFilename().toLowerCase().contains(".wav")
				|| !"audio/wav".equals(songFile.getContentType())) {
			
			throw new InvalidInputException("Only WAV files are supportedS");
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

		songRepository.save(song);

		if (oldPath != null) {
			Files.deleteIfExists(Paths.get(oldPath));
		}
	}

	public Resource streamSong(String songId) throws IOException {

		Song song = songRepository.findById(songId).orElse(null);

		if (song != null && song.getFilePath() != null) {

			if (Files.exists(Path.of(song.getFilePath()))) {

				Path filePath = Paths.get(song.getFilePath());
				return new FileSystemResource(filePath);
			}

		}
		
		throw new ResourceNotFoundException("Song not found");
	}
}
