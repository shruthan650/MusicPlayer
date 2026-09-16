package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class SongService {

	final SongRepository songRepository;
	final PlaylistRepository playlistRepository;

	SongService(SongRepository repository, PlaylistRepository playlistRepository) {
		this.songRepository = repository;
		this.playlistRepository = playlistRepository;
	}

	public List<Song> getAllSongs() {
		return songRepository.findAll();
	}

	public Song addSong(Song song) {
		return songRepository.save(song);
	}

	public Song getSongById(String songId) {
		return songRepository.findById(songId).orElse(null);
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
			return;
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
			return;
		}
	}

	public void uploadSong(MultipartFile songFile, String songId) throws IOException {

		Song song = songRepository.findById(songId).orElse(null);

		String oldPath = song.getFilePath();

		if (songFile.isEmpty() || songFile.getOriginalFilename().isEmpty()
				|| !songFile.getOriginalFilename().toLowerCase().contains(".wav")
				|| !"audio/wav".equals(songFile.getContentType())) {
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
		
		return null;
	}

}
