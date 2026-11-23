package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.LikeService;

@RequestMapping("/api")
@RestController
public class LikeController {

	private final LikeService likeService;

	public LikeController(LikeService likeService) {
		this.likeService = likeService;
	}

	@PostMapping("/like/{songId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> likeSong(@PathVariable String songId) {
		likeService.likeSong(songId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/like/{songId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> unlikeSong(@PathVariable String songId) {
		likeService.unlikeSong(songId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/liked")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<List<Song>> getLikedSongs() {
		return ResponseEntity.ok(likeService.getLikedSongs());
	}
}