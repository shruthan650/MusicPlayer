package com.shruthan.musicplayer.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;

@Service
public class SecurityService {

	private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

	private final PlaylistRepository playlistRepository;
	private final SongRepository songRepository;
	private final UserRepository userRepository;

	public SecurityService(
			PlaylistRepository playlistRepository,
			SongRepository songRepository,
			UserRepository userRepository) {

		this.playlistRepository = playlistRepository;
		this.songRepository = songRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public boolean isPlaylistOwner(String playlistId) {

		logger.debug("Checking playlist ownership for: {}", playlistId);

		Optional<Playlist> playlist = playlistRepository.findById(playlistId);

		if (playlist.isEmpty()) {
			logger.warn("Playlist not found during ownership check: {}", playlistId);
			return false;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		User user = userRepository.findByUserEmail(email);

		if (user == null) {
			logger.warn("Authenticated user not found during playlist ownership check");
			return false;
		}

		boolean isOwner = user.getId().equals(playlist.get().getOwnerId());

		logger.debug("Playlist ownership check for {}: {}", playlistId, isOwner);

		return isOwner;
	}

	@Transactional
	public boolean isSongOwner(String songId) {

		logger.debug("Checking song ownership for: {}", songId);

		Optional<Song> song = songRepository.findById(songId);

		if (song.isEmpty()) {
			logger.warn("Song not found during ownership check: {}", songId);
			return false;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		User user = userRepository.findByUserEmail(email);

		if (user == null) {
			logger.warn("Authenticated user not found during song ownership check");
			return false;
		}

		boolean isOwner = user.getId().equals(song.get().getOwnerId());

		logger.debug("Song ownership check for {}: {}", songId, isOwner);

		return isOwner;
	}

	@Transactional(readOnly = true)
	public User getCurrentUser() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		logger.debug("Fetching current authenticated user");

		User user = userRepository.findByUserEmail(email);

		if (user == null) {
			logger.warn("Authenticated user not found: {}", email);
		}

		return user;
	}
}