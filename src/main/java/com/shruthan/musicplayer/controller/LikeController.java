package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void likeSong(@PathVariable String songId) {
        likeService.likeSong(songId);
    }
    
    @DeleteMapping("/like/{songId}")
    @PreAuthorize("isAuthenticated()")
    public void unlikeSong(@PathVariable String songId) {
        likeService.unlikeSong(songId);
    }
    
    @GetMapping("/liked")
    @PreAuthorize("isAuthenticated()")
    public List<Song> getLikedSongs() {
        return likeService.getLikedSongs();
    }
}
