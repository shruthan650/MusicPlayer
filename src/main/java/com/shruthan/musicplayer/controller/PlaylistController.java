package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.service.PlaylistService;

@RestController
@RequestMapping("/api")
public class PlaylistController {

	final PlaylistService service;

	PlaylistController(PlaylistService service) {
		this.service = service;
	}
	
	@GetMapping("/playlists")
	public List<Playlist> getAllPlaylists() {
		return service.getAllPlaylists();
	}
	
	@GetMapping("/playlist/{playlistId}")
	public Playlist getPlaylistById(@PathVariable String playlistId) {
		return service.getPlaylistById(playlistId);
	}
	
	@PostMapping("/playlist")
	public Playlist createPlaylist(@RequestBody Playlist playlist) {
		return service.createPlaylist(playlist);
	}
	
	@PutMapping("/playlist/{id}")
	public Playlist updatePlaylistById(@RequestBody Playlist playlist, @PathVariable String id) {
		return service.updatePlaylistById(playlist, id);
	}
	
	@DeleteMapping("/playlist/{id}")
	public void deletePlaylistById(@PathVariable String playlistId) {
		service.deletePlaylistById(playlistId);
	}
	
	@PostMapping("/playlist/{playlistId}/song/{songId}")
	public void addSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		service.addSongToPlaylist(songId, playlistId);
	}
	
	@DeleteMapping("/playlist/{playlistId}/song/{songId}")
	public Playlist removeSongFromPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		return service.removeSongFromPlaylist(songId, playlistId);
	}
}
