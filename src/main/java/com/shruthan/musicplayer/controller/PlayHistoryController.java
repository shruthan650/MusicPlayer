package com.shruthan.musicplayer.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shruthan.musicplayer.model.PlayHistory;
import com.shruthan.musicplayer.service.PlayHistoryService;

@RestController
@RequestMapping("/api/user")
public class PlayHistoryController {

	private final PlayHistoryService playHistoryService;

	public PlayHistoryController(PlayHistoryService playHistoryService) {
		this.playHistoryService = playHistoryService;
	}

	@GetMapping("/history")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<PlayHistory>> getHistory(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		return ResponseEntity.ok(playHistoryService.getHistory(page, size));
	}

	@PostMapping("/history/{songId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> addToHistory(@PathVariable String songId) {

		playHistoryService.addToHistory(songId);

		return ResponseEntity.ok().build();
	}

	@PutMapping("/history/{songId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> updatePosition(@PathVariable String songId, @RequestParam long position) {

		playHistoryService.updatePosition(songId, position);

		return ResponseEntity.ok().build();
	}

	@GetMapping("/history/{songId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<PlayHistory> getPosition(@PathVariable String songId) {

		return ResponseEntity.ok(playHistoryService.getPosition(songId));
	}
}