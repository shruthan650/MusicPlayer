package com.shruthan.musicplayer.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.PlayHistory;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlayHistoryRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class PlayHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(PlayHistoryService.class);

	private final SecurityService securityService;
	private final SongRepository songRepository;
	private final PlayHistoryRepository playHistoryRepository;

	public PlayHistoryService(
			SecurityService securityService, 
			SongRepository songRepository,
			PlayHistoryRepository playHistoryRepository) {

		this.playHistoryRepository = playHistoryRepository;
		this.securityService = securityService;
		this.songRepository = songRepository;
	}

	@Transactional(readOnly = true)
	public Page<PlayHistory> getHistory(int page, int size) {

		User user = securityService.getCurrentUser();

		logger.debug("Fetching play history for user: {}, page: {}, size: {}", user.getId(), page, size);

		Pageable pageable = PageRequest.of(page, size);

		Page<PlayHistory> history = playHistoryRepository.findByUserIdOrderByPlayedAtDesc(user.getId(), pageable);

		logger.debug("Fetched {} play history records for user: {}", history.getNumberOfElements(), user.getId());

		return history;
	}

	@Transactional
	public void addToHistory(String songId) {

		User user = securityService.getCurrentUser();

		logger.info("Adding song {} to play history for user {}", songId, user.getId());

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found while adding to history: {}", songId);
			return new ResourceNotFoundException("Song not found");
		});

		PlayHistory history = playHistoryRepository.findByUserIdAndSongId(user.getId(), songId)
				.orElseGet(PlayHistory::new);

		history.setUserId(user.getId());
		history.setSongId(songId);

		if (history.getId() == null) {
			history.setPosition(0);
		}

		history.setPlayedAt(new Date());

		playHistoryRepository.save(history);

		logger.info("Play history updated successfully for user {}, song {}", user.getId(), songId);
	}

	@Transactional
	public void updatePosition(String songId, long position) {

		User user = securityService.getCurrentUser();

		logger.debug("Updating playback position for user {}, song {}, position {}", user.getId(), songId, position);

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found while updating position: {}", songId);
			return new ResourceNotFoundException("Song Not Found");
		});

		PlayHistory playHistory = playHistoryRepository.findByUserIdAndSongId(user.getId(), songId)
				.orElseGet(PlayHistory::new);

		playHistory.setUserId(user.getId());
		playHistory.setSongId(songId);
		playHistory.setPosition(position);
		playHistory.setPlayedAt(new Date());

		playHistoryRepository.save(playHistory);

		logger.debug("Playback position updated successfully for user {}, song {}", user.getId(), songId);
	}

	@Transactional(readOnly = true)
	public PlayHistory getPosition(String songId) {

		User user = securityService.getCurrentUser();

		logger.debug("Fetching playback position for user {}, song {}", user.getId(), songId);

		return playHistoryRepository.findByUserIdAndSongId(user.getId(), songId).orElseThrow(() -> {
			logger.warn("No playback history found for user {}, song {}", user.getId(), songId);
			return new ResourceNotFoundException("No playback history found");
		});
	}
}