package com.shruthan.musicplayer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.repository.PlaylistRepository;

@Service
public class PlaylistService {
	
	final PlaylistRepository repository;
	
	public PlaylistService(PlaylistRepository repository) {
		this.repository = repository;
	}

	public List<Playlist> getAllPlaylists() {
		return repository.findAll();
	}
	
	public Playlist getPlaylistById(String id) {
		return repository.findById(id).orElse(null);
	}
	
	public Playlist createPlaylist(Playlist playlist) {
		return repository.save(playlist);
	}
	
	public Playlist updatePlaylistById(Playlist playlist, String id) {
		return repository.save(playlist);
	}
	
	public void deletePlaylistById(String playlistId) {
		
		Playlist playlist = repository.findById(playlistId).orElse(null);
		
		repository.delete(playlist);
	}
	
	public Playlist addSongToPlaylist(String songId, String playlistId) {
		
		Playlist playlist = repository.findById(playlistId).orElse(null);
		
		if (!songPresent(songId, playlistId)) {
			playlist.getSongIds().add(songId);
		}
		
		return repository.save(playlist);	
		
	}
	
	private boolean songPresent(String songId, String playlistId) {
		Playlist playlist = repository.findById(playlistId).orElse(null);

		return playlist.getSongIds().contains(songId);
	}
	
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {
		
		Playlist playlist = repository.findById(playlistId).orElse(null);
		
		playlist.getSongIds().remove(songId);
		
		return playlist;
	}
	
	
}
