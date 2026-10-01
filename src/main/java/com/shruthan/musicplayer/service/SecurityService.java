package com.shruthan.musicplayer.service;


import java.util.Optional;

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

	final PlaylistRepository playlistRepository;
	final SongRepository songRepository;
	final UserRepository userRepository;

	SecurityService(
			PlaylistRepository playlistRepository,
			SongRepository songRepository,
			UserRepository userRepository) {
		
		this.playlistRepository = playlistRepository;
		this.songRepository = songRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public boolean isPlaylistOwner(String playlistId) {

		Optional<Playlist> playlist = playlistRepository.findById(playlistId);

		if (playlist.isEmpty()) {
			return false;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		User user = userRepository.findByUserEmail(email);

		if (user == null) {
			return false;
		}

		return (user.getId().equals(playlist.get().getOwnerId()));
	}

	@Transactional
	public boolean isSongOwner(String songId) {

		Optional<Song> song = songRepository.findById(songId);

		if (song.isEmpty()) {
			return false;
		}

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		String email = authentication.getName();

		User user = userRepository.findByUserEmail(email);

		if (user == null) {
			return false;
		}

		return (user.getId().equals(song.get().getOwnerId()));
	}
	
	@Transactional
	public User getCurrentUser() {
	    Authentication authentication =
	            SecurityContextHolder.getContext().getAuthentication();

	    String email = authentication.getName();

	    return userRepository.findByUserEmail(email);
	}
}
