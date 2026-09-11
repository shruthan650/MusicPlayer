package com.shruthan.musicplayer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class PlaylistService {
	
	final PlaylistRepository playlistRepo;
	final SongRepository songRepo;
	
	public PlaylistService(PlaylistRepository playlistRepo, SongRepository songRepo) {
		this.playlistRepo = playlistRepo;
		this.songRepo = songRepo;
	}

	public List<Playlist> getAllPlaylists() {
		return playlistRepo.findAll();
	}
	
	public Playlist getPlaylistById(String id) {
		return playlistRepo.findById(id).orElse(null);
	}
	
	public Playlist createPlaylist(Playlist playlist) {
		return playlistRepo.save(playlist);
	}
	
	public Playlist updatePlaylistById(Playlist playlist, String id) {
		return playlistRepo.save(playlist);
	}
	
	public void deletePlaylistById(String playlistId) {
		
		Playlist playlist = playlistRepo.findById(playlistId).orElse(null);
		
		playlistRepo.delete(playlist);
	}
	
	public Playlist addSongToPlaylist(String songId, String playlistId) {
		
		Playlist playlist = playlistRepo.findById(playlistId).orElse(null);
		Song song = songRepo.findById(songId).orElse(null);
		
		if (playlist != null && song != null) {
			
			if (!playlist.getSongIds().contains(songId)) {
				playlist.getSongIds().add(songId);				
			}
			return playlistRepo.save(playlist);
		}
		
		return null;
		
	}
	
	
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {
		
		Playlist playlist = playlistRepo.findById(playlistId).orElse(null);
		
		playlist.getSongIds().remove(songId);
		
		playlistRepo.save(playlist);
		
		return playlist;
	}
	
	
}
