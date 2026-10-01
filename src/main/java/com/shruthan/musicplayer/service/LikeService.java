package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;

@Service
public class LikeService {

	private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

	private final SecurityService securityService;
	private final SongRepository songRepository;
	private final UserRepository userRepository;

	public LikeService(
			SecurityService securityService,
			SongRepository songRepository,
			UserRepository userRepository) {

		this.securityService = securityService;
		this.songRepository = songRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public void likeSong(String songId) {

		User user = securityService.getCurrentUser();

		logger.info("User {} is liking song {}", user.getId(), songId);

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found while liking: {}", songId);
			return new ResourceNotFoundException("Song not found");
		});

		if (user.getLikedSongIds() == null) {
			user.setLikedSongIds(new HashSet<>());
		}

		user.getLikedSongIds().add(songId);

		userRepository.save(user);

		logger.info("Song {} liked successfully by user {}", songId, user.getId());
	}

	@Transactional
	public void unlikeSong(String songId) {

		User user = securityService.getCurrentUser();

		logger.info("User {} is unliking song {}", user.getId(), songId);

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found while unliking: {}", songId);
			return new ResourceNotFoundException("Song not found");
		});

		if (user.getLikedSongIds() != null) {
			user.getLikedSongIds().remove(songId);
		}

		userRepository.save(user);

		logger.info("Song {} unliked successfully by user {}", songId, user.getId());
	}

	@Transactional(readOnly = true)
	public List<Song> getLikedSongs() {

		User user = securityService.getCurrentUser();

		logger.debug("Fetching liked songs for user {}", user.getId());

		List<Song> songs = new ArrayList<>();

		if (user.getLikedSongIds() == null) {
			logger.debug("User {} has no liked songs", user.getId());
			return songs;
		}

		for (String songId : user.getLikedSongIds()) {
			songRepository.findById(songId).ifPresent(songs::add);
		}

		logger.debug("Fetched {} liked songs for user {}", songs.size(), user.getId());

		return songs;
	}
}