package com.shruthan.musicplayer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class SongService {
	
	final SongRepository repository;

	SongService(SongRepository repository) {
		this.repository = repository;
	}

	public List<Song> getAllSongs() {
		return repository.findAll();
	}
	
	public Song addSong(Song song) {
		return repository.save(song);
	}
	
	public Song getSongById(String id) {
		return repository.findById(id).orElse(null);
	}
	
	public void updateSongById(Song song, String id) {
		repository.save(song);
	}
	
	public void deleteSongById(String id) {
		repository.deleteById(id);
	}
	
}
