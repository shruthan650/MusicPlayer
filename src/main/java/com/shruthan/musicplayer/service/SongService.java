package com.shruthan.musicplayer.service;

import com.shruthan.musicplayer.dto.ArtistStatistics;
import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlayHistoryRepository;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.*;

@Service
public class SongService {

	private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of("mp3", "wav");
	private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

	private final SongRepository songRepository;
	private final PlaylistRepository playlistRepository;
	private final UserRepository userRepository;
	private final PlayHistoryRepository playHistoryRepository;
	private final SecurityService securityService;
	private final StorageService storageService;

	public SongService(
			SongRepository songRepository,
			PlaylistRepository playlistRepository,
			UserRepository userRepository,
			PlayHistoryRepository playHistoryRepository,
			SecurityService securityService,
			StorageService storageService) {
		this.songRepository = songRepository;
		this.playlistRepository = playlistRepository;
		this.userRepository = userRepository;
		this.playHistoryRepository = playHistoryRepository;
		this.securityService = securityService;
		this.storageService = storageService;
	}

	@Transactional(readOnly = true)
	public Page<Song> getAllSongs(Pageable pageable) {
		return songRepository.findAll(pageable);
	}

	@Transactional
	public Song addSong(Song song) {
		User user = securityService.getCurrentUser();
		song.setOwnerId(user.getId());
		return songRepository.save(song);
	}

	@Transactional(readOnly = true)
	public Song getSongById(String songId) {
		return songRepository.findById(songId)
				.orElseThrow(() -> new ResourceNotFoundException("Song with ID " + songId + " not found"));
	}

	@Transactional
	public void updateSongById(Song updatedSong, String songId) {
		Song songInRepo = getSongById(songId);

		songInRepo.setAlbumName(updatedSong.getAlbumName());
		songInRepo.setArtistName(updatedSong.getArtistName());
		songInRepo.setDuration(updatedSong.getDuration());
		songInRepo.setGenre(updatedSong.getGenre());
		songInRepo.setTitle(updatedSong.getTitle());

		songRepository.save(songInRepo);
	}

	@Transactional
	public void deleteSongById(String songId) {
		Song song = getSongById(songId);

		storageService.deleteFileIfExists(song.getFilePath());
		storageService.deleteFileIfExists(song.getCoverImagePath());

		songRepository.deleteById(songId);
		playHistoryRepository.deleteBySongId(songId);

		playlistRepository.pullSongFromAllPlaylists(songId);
		userRepository.pullSongFromAllLikedLists(songId);
	}

	@Transactional
	public void uploadSongFile(String songId, MultipartFile file) {
		Song song = getSongById(songId);
		String oldFilePath = song.getFilePath();

		try {
			String newFilePath = storageService.storeSongFile(file, ALLOWED_AUDIO_EXTENSIONS);

			File audioFileOnDisk = new File(newFilePath);
			AudioFile audioFile = AudioFileIO.read(audioFileOnDisk);
			AudioHeader audioHeader = audioFile.getAudioHeader();
			long durationInMillis = audioHeader.getTrackLength() * 1000L;

			song.setDuration(durationInMillis);
			song.setFilePath(newFilePath);
			songRepository.save(song);

			storageService.deleteFileIfExists(oldFilePath);

		} catch (Exception e) {
			throw new InvalidInputException("Failed to process audio file: " + e.getMessage());
		}
	}

	@Transactional
	public Song uploadCoverImage(String songId, MultipartFile file) {
		Song song = getSongById(songId);
		String oldCoverPath = song.getCoverImagePath();

		try {
			String newCoverPath = storageService.storeCoverFile(file, ALLOWED_IMAGE_EXTENSIONS);
			song.setCoverImagePath(newCoverPath);
			Song savedSong = songRepository.save(song);

			storageService.deleteFileIfExists(oldCoverPath);
			return savedSong;
		} catch (Exception e) {
			throw new InvalidInputException("Failed to store image cover: " + e.getMessage());
		}
	}

	@Transactional(readOnly = true)
	public Resource getSongResource(String songId) {
		Song song = getSongById(songId);
		return storageService.loadAsResource(song.getFilePath());
	}

	@Transactional(readOnly = true)
	public Resource getCoverImageResource(String songId) {
		Song song = getSongById(songId);
		return storageService.loadAsResource(song.getCoverImagePath());
	}

	@Transactional
	public void incrementPlayCount(String songId) {
		Song song = getSongById(songId);
		song.setPlayCount(song.getPlayCount() + 1);
		songRepository.save(song);
	}

	@Transactional(readOnly = true)
	public Page<Song> searchSongs(String query, Pageable pageable) {
		return songRepository.searchSongs(query, pageable);
	}

	@Transactional(readOnly = true)
	public Page<Song> getMostPlayedSongs(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("playCount"), Sort.Order.asc("title")));
		return songRepository.findAllByOrderByPlayCountDesc(pageable);
	}

	@Transactional(readOnly = true)
	public Page<Song> getMostPlayedSongsByGenre(String genre, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("playCount"), Sort.Order.asc("title")));
		return songRepository.findByGenre(genre, pageable);
	}

	@Transactional(readOnly = true)
	public ArtistStatistics getArtistStatistics() {
		User artist = securityService.getCurrentUser();
		List<Song> songs = songRepository.findByOwnerId(artist.getId());

		long totalSongs = songs.size();
		long totalPlays = songs.stream().mapToLong(Song::getPlayCount).sum();

		Optional<Song> mostPlayedOpt = songs.stream().max(Comparator.comparingLong(Song::getPlayCount));

		String mostPlayedSong = mostPlayedOpt.map(Song::getTitle).orElse(null);
		long mostPlayedCount = mostPlayedOpt.map(Song::getPlayCount).orElse(0L);

		return new ArtistStatistics(totalSongs, totalPlays, mostPlayedSong, mostPlayedCount);
	}

	@Transactional(readOnly = true)
	public List<Song> getRecommendations() {
		User user = securityService.getCurrentUser();
		Set<String> likedSongIds = user.getLikedSongIds();

		if (likedSongIds.isEmpty()) {
			return List.of();
		}

		// Optimized batch query instead of N+1 loop
		List<Song> likedSongs = songRepository.findAllById(likedSongIds);

		List<String> genres = likedSongs.stream()
				.map(Song::getGenre)
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		if (genres.isEmpty()) {
			return List.of();
		}

		return songRepository.findByGenreInOrderByPlayCountDesc(genres)
				.stream()
				.filter(song -> !likedSongIds.contains(song.getId()))
				.limit(10)
				.toList();
	}
}