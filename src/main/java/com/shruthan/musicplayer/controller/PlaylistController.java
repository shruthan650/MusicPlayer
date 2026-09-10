package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
	
	@GetMapping("/playlist/{id}")
	public Playlist getAllPlaylists(@PathVariable String id) {
		return service.getPlaylistById(id);
	}
	
	@GetMapping("/playlist")
	public Playlist createPlaylist(@RequestBody Playlist playlist) {
		return service.createPlaylist(playlist);
	}
	
	@PutMapping("/playlist/{id}")
	public Playlist updatePlaylistById(@RequestBody Playlist playlist, @PathVariable String id) {
		return service.updatePlaylistById(playlist, id);
	}
	
	@DeleteMapping("/playlist/{id}")
	public void deletePlaylistById(@PathVariable String id) {
		service.deletePlaylistById(id);
	}
	
	@PutMapping("/playlist/{id}/song/{id}")
	public void addSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		service.addSongToPlaylist(songId, playlistId);
	}
	
	@PutMapping("/playlist/{id}/song/{id}")
	public Playlist removeSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId) {
		return service.addSongToPlaylist(songId, playlistId);
	}
}
