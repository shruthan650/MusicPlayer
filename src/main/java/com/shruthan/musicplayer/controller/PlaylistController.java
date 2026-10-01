package com.shruthan.musicplayer.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.PlaylistService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistController {

	private final PlaylistService service;

	public PlaylistController(PlaylistService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<Page<Playlist>> getAllPlaylists(@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(service.getAllPlaylists(pageable));
	}

	@GetMapping("/{playlistId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<List<Song>> getPlaylistById(@PathVariable String playlistId) {
		return ResponseEntity.ok(service.getPlaylistById(playlistId));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ARTIST', 'USER')")
	public ResponseEntity<Playlist> createPlaylist(@Valid @RequestBody Playlist playlist) {
		return ResponseEntity.status(HttpStatus.CREATED).body(playlist);
	}

	@PutMapping("/{playlistId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Playlist> updatePlaylistById(@Valid @RequestBody Playlist playlist,
			@PathVariable String playlistId) {
		return ResponseEntity.ok(service.updatePlaylistById(playlist, playlistId));

	}

	@DeleteMapping("/{playlistId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Void> deletePlaylistById(@PathVariable String playlistId) {
		service.deletePlaylistById(playlistId);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{playlistId}/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Void> addSongToPlaylist(@PathVariable String playlistId, @PathVariable String songId) {

		service.addSongToPlaylist(songId, playlistId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{playlistId}/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Playlist> removeSongFromPlaylist(@PathVariable String playlistId,
			@PathVariable String songId) {
		return ResponseEntity.ok(service.removeSongFromPlaylist(songId, playlistId));

	}

	@PutMapping("/{playlistId}/song/{songId}/{position}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Playlist> moveSongToDesiredPosition(@PathVariable String playlistId,
			@PathVariable String songId, @PathVariable int position) {
		return ResponseEntity.ok(service.moveSongToDesiredPosition(playlistId, songId, position));

	}

	@PostMapping("/{playlistId}/song/{songId}/next")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isPlaylistOwner(#playlistId)")
	public ResponseEntity<Playlist> insertNewSongAtPosition(@PathVariable String playlistId,
			@PathVariable String songId, @RequestParam String currentSongId) {
		return ResponseEntity.ok(service.insertNewSongAtPosition(playlistId, songId, currentSongId));
	}
}