package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.exception.ResourceNotFoundException;
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
	
	public List<Song> getPlaylistById(String id) {
		
		Playlist playlist = playlistRepo.findById(id).orElse(null);
		
		if (playlist == null) {
			throw new ResourceNotFoundException("Playlist Not Found");
		}
		
		Set<String> songIds = playlist.getSongIds();
		
		List<Song> songList = new ArrayList<Song>();
		
		for(String songId : songIds) {
			Song song = songRepo.findById(songId).orElse(null);
			if (song != null) {
				songList.add(song);
			}
		}
		
		return songList;
		
	}
	
	public Playlist createPlaylist(Playlist playlist) {
		return playlistRepo.save(playlist);
	}
	
	public Playlist updatePlaylistById(Playlist playlist, String id) {
		return playlistRepo.save(playlist);
	}
	
	public void deletePlaylistById(String playlistId) {
		
		Playlist playlist = playlistRepo.findById(playlistId).orElse(null);
		
		if (playlist == null) {
			throw new ResourceNotFoundException("Can't delete a non-existent playlist");
		}
		
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
		
		throw new ResourceNotFoundException("Playlist Not Found");
		
	}
	
	
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {
		
		Playlist playlist = playlistRepo.findById(playlistId).orElse(null);
		
		playlist.getSongIds().remove(songId);
		
		playlistRepo.save(playlist);
		
		return playlist;
	}
	
	
}
