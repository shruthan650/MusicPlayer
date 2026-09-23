package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
	final UserRepository userRepository;
	final SecurityService securityService;

	SongService(SongRepository repository, PlaylistRepository playlistRepository, UserRepository userRepository,
			SecurityService securityService) {

		this.songRepository = repository;
		this.playlistRepository = playlistRepository;
		this.securityService = securityService;
		this.userRepository = userRepository;
	}

	public org.springframework.data.domain.Page<Song> getAllSongs(org.springframework.data.domain.Pageable pageable) {

		return songRepository.findAll(pageable);
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
			
			List<User> users = userRepository.findByLikedSongIdsContaining(songId);

			for (User user : users) {
			    user.getLikedSongIds().remove(songId);
			    userRepository.save(user);
			}
			
		} else {
			throw new ResourceNotFoundException("Song not found");
		}
	}

	public void uploadSong(MultipartFile songFile, String songId)
			throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {

		Song song = songRepository.findById(songId).orElseThrow(() -> new ResourceNotFoundException("Song not found"));

		String oldPath = song.getFilePath();

		String originalFileName = songFile.getOriginalFilename();

		if (songFile.isEmpty() || originalFileName == null || originalFileName.isBlank()) {

			throw new InvalidInputException("Invalid audio file");
		}

		String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

		String contentType = songFile.getContentType();

		if (!extension.equals("wav") && !extension.equals("mp3")) {
			throw new InvalidInputException("Only WAV and MP3 files are supported");
		}

		if (extension.equals("wav") && !"audio/wav".equalsIgnoreCase(contentType)) {

			throw new InvalidInputException("Invalid WAV file type");
		}

		if (extension.equals("mp3") && !"audio/mpeg".equalsIgnoreCase(contentType)) {

			throw new InvalidInputException("Invalid MP3 file type");
		}

		Path folder = Paths.get("songs");

		Files.createDirectories(folder);

		String fileName = UUID.randomUUID() + "." + extension;

		Path filePath = folder.resolve(fileName);

		Files.write(filePath, songFile.getBytes());

		// Read audio duration
		AudioFile audioFile = AudioFileIO.read(filePath.toFile());

		AudioHeader audioHeader = audioFile.getAudioHeader();

		int durationInSeconds = audioHeader.getTrackLength();

		long durationInMilliseconds = durationInSeconds * 1000L;

		song.setDuration(durationInMilliseconds);

		song.setFilePath(filePath.toString());

		songRepository.save(song);

		// Delete old audio file
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

	public Page<Song> searchSongs(String query, Pageable pageable) {
		return songRepository.searchSongs(query, pageable);
	}

}
