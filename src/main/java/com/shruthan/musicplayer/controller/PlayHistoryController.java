package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.shruthan.musicplayer.model.PlayHistory;
import com.shruthan.musicplayer.service.PlayHistoryService;

public class PlayHistoryController {
	
	private final PlayHistoryService playHistoryService;
	
	public PlayHistoryController(PlayHistoryService playHistoryService) {
		this.playHistoryService = playHistoryService;
	}

	@GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public List<PlayHistory> getHistory() {
        return playHistoryService.getHistory();
    }
    
    @PostMapping("/history/{songId}")
    @PreAuthorize("isAuthenticated()")
    public void addToHistory(@PathVariable String songId) {
        playHistoryService.addToHistory(songId);
    }
    
    @PutMapping("/history/{songId}")
    @PreAuthorize("isAuthenticated()")
    public void updatePosition(@PathVariable String songId, @RequestParam long position) {
    	playHistoryService.updatePosition(songId, position);	
    }
    
    @GetMapping("/history/{songId}")
    @PreAuthorize("isAuthenticated()")
    public PlayHistory getPosition(@PathVariable String songId) {
    	return playHistoryService.getPosition(songId);
    }
}
