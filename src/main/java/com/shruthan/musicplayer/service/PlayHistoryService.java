package com.shruthan.musicplayer.service;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.PlayHistory;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlayHistoryRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class PlayHistoryService {
	
	private final SecurityService securityService;
	private final SongRepository songRepository;
	private final PlayHistoryRepository playHistoryRepository;
	
	public PlayHistoryService(
			SecurityService securityService,
			SongRepository songRepository,
			PlayHistoryRepository playHistoryRepository
			) {
		this.playHistoryRepository = playHistoryRepository;
		this.securityService = securityService;
		this.songRepository = songRepository;
	}
	
	public List<PlayHistory> getHistory() {

	    User user = securityService.getCurrentUser();

	    return playHistoryRepository
	            .findByUserIdOrderByPlayedAtDesc(user.getId());
	}

	public void addToHistory(String songId) {

	    User user = securityService.getCurrentUser();

	    songRepository.findById(songId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Song not found"));

	    PlayHistory history = playHistoryRepository
	    		.findByUserIdAndSongId(
	    				user.getId(),
	    				songId
	    		)
	    		.orElseGet(PlayHistory::new);

	    history.setUserId(user.getId());
	    history.setSongId(songId);
	    
	    if (history.getId() == null) {
			history.setPosition(0);
		}
	    
	    history.setPlayedAt(new Date());

	    playHistoryRepository.save(history);
	}
	
	public void updatePosition(String songId, long position) {
		
		User user = securityService.getCurrentUser();
		
		songRepository.findById(songId)
				.orElseThrow(() -> 
						new ResourceNotFoundException("Song Not Found"));
		
		PlayHistory playHistory = playHistoryRepository
				.findByUserIdAndSongId(
						user.getId(), 
						songId
						)
				.orElseGet(PlayHistory::new);
		
		playHistory.setUserId(user.getId());
		playHistory.setSongId(songId);
		playHistory.setPosition(position);
		playHistory.setPlayedAt(new Date());
		
		playHistoryRepository.save(playHistory);
	}
	
	public PlayHistory getPosition(String songId) {
		
		User user = securityService.getCurrentUser();
		
		return playHistoryRepository
				.findByUserIdAndSongId(
						user.getId(), 
						songId).
				orElseThrow(() -> 
					new ResourceNotFoundException("No playback history found")
				);
	}
}
