package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class PlaylistService {
	
	private final PlaylistRepository playlistRepo;
	private final SongRepository songRepo;
	private final SecurityService securityService;
	
	public PlaylistService(
			PlaylistRepository playlistRepo,
			SongRepository songRepo,
			SecurityService securityService) {
		
		this.playlistRepo = playlistRepo;
		this.songRepo = songRepo;
		this.securityService = securityService;
	}
	
	public List<Playlist> getAllPlaylists() {

	    User user = securityService.getCurrentUser();

	    if (user == null) {
	        throw new ResourceNotFoundException("User not found");
	    }

	    return playlistRepo.findByOwnerId(user.getId());
	}
	
	public List<Song> getPlaylistById(String playlistId) {

	    Playlist playlist = playlistRepo.findById(playlistId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Playlist not found"));

	    if (playlist.getSongIds() == null || playlist.getSongIds().isEmpty()) {
	        return new ArrayList<>();
	    }

	    List<Song> songs = new ArrayList<>();

	    for (String songId : playlist.getSongIds()) {

	        songRepo.findById(songId)
	                .ifPresent(songs::add);
	    }

	    return songs;
	}
	
	public Playlist createPlaylist(Playlist playlist) {
		
		User user = securityService.getCurrentUser();
		playlist.setOwnerId(user.getId());
		
		return playlistRepo.save(playlist);
	}
	
	public Playlist updatePlaylistById(Playlist playlist, String id) {

	    Playlist existingPlaylist = playlistRepo.findById(id)
	            .orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

	    existingPlaylist.setName(playlist.getName());

	    return playlistRepo.save(existingPlaylist);
	}
	
	public void deletePlaylistById(String id) {

	    Playlist playlist = playlistRepo.findById(id)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Playlist not found"));

	    playlistRepo.delete(playlist);
	}
	
	public void addSongToPlaylist(String songId, String playlistId) {

	    Playlist playlist = playlistRepo.findById(playlistId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Playlist not found"));

	    songRepo.findById(songId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Song not found"));

	    if (playlist.getSongIds() == null) {
	        playlist.setSongIds(new HashSet<>());
	    }

	    playlist.getSongIds().add(songId);

	    playlistRepo.save(playlist);
	}
	
	
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {

	    Playlist playlist = playlistRepo.findById(playlistId)
	            .orElseThrow(() ->
	                    new ResourceNotFoundException("Playlist not found"));

	    if (playlist.getSongIds() != null) {
	        playlist.getSongIds().remove(songId);
	    }

	    return playlistRepo.save(playlist);
	}
	
	
}
