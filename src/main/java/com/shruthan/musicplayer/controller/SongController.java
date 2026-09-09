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

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.SongService;

@RestController
@RequestMapping("/api")
public class SongController {
	
	final SongService service;
	
	public SongController(SongService service) {
		this.service = service;
	}

	@GetMapping("/songs")
	public List<Song> getAllSongs() {
		return service.getAllSongs();
	}
	
	@PostMapping("/song")
	public Song addSong(@RequestBody Song song) {
		return service.addSong(song);
	}

	@GetMapping("/song/{id}")
	public Song getSongById(@PathVariable String id) {
		return service.getSongById(id);
	}
	
	@PutMapping("/song/{id}")
	public void updateSongById(@PathVariable String id, @RequestBody Song song) {
		service.updateSongById(song, id);
	}
	
	@DeleteMapping("/song/{id}")
	public void deleteSongById(@PathVariable String id) {
		service.deleteSongById(id);
	}
}