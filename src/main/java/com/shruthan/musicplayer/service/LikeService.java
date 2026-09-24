package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;

public class LikeService {
	
	private final SecurityService securityService;
	private final SongRepository songRepository;
	private final UserRepository userRepository;
	
	public LikeService(
			SecurityService securityService,
			SongRepository songRepository,
			UserRepository userRepository
			) {
		this.securityService = securityService;
		this.songRepository = songRepository;
		this.userRepository = userRepository;
	}

	public void likeSong(String songId) {

	    User user = securityService.getCurrentUser();

	    songRepository.findById(songId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Song not found"));

	    if (user.getLikedSongIds() == null) {
	        user.setLikedSongIds(new HashSet<>());
	    }

	    user.getLikedSongIds().add(songId);

	    userRepository.save(user);
	}

	public void unlikeSong(String songId) {

	    User user = securityService.getCurrentUser();

	    if (user.getLikedSongIds() != null) {
	        user.getLikedSongIds().remove(songId);
	    }

	    userRepository.save(user);
	}
	
	public List<Song> getLikedSongs() {

	    User user = securityService.getCurrentUser();

	    List<Song> songs = new ArrayList<>();

	    if (user.getLikedSongIds() == null) {
	        return songs;
	    }

	    for (String songId : user.getLikedSongIds()) {

	        songRepository.findById(songId)
	                .ifPresent(songs::add);
	    }

	    return songs;
	}
}
