package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.PlaylistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class PlaylistController {

	private final PlaylistService service;

	PlaylistController(PlaylistService service) {
		this.service = service;
	}
	
	@GetMapping("/playlists")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public List<Playlist> getAllPlaylists() {
		return service.getAllPlaylists();
	}
	
	@GetMapping("/playlist/{playlistId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public List<Song> getPlaylistById(@PathVariable String playlistId) {
		return service.getPlaylistById(playlistId);
	}
	
	@PostMapping("/playlist")
	@PreAuthorize("hasAnyRole('ADMIN', 'ARTIST', 'USER')")
	public Playlist createPlaylist(@Valid @RequestBody Playlist playlist) {
		return service.createPlaylist(playlist);
	}
	
	@PutMapping("/playlist/{id}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#id)")
	public Playlist updatePlaylistById(@Valid @RequestBody Playlist playlist, @PathVariable String id) {
		return service.updatePlaylistById(playlist, id);
	}
	
	@DeleteMapping("/playlist/{id}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public void deletePlaylistById(@PathVariable String playlistId) {
		service.deletePlaylistById(playlistId);
	}
	
	@PostMapping("/playlist/{playlistId}/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public void addSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		service.addSongToPlaylist(songId, playlistId);
	}
	
	@DeleteMapping("/playlist/{playlistId}/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public Playlist removeSongFromPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		return service.removeSongFromPlaylist(songId, playlistId);
	}
}
