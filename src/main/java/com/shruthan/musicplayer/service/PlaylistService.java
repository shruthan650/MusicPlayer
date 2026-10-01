package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;

@Service
public class PlaylistService {

	private final PlaylistRepository playlistRepository;
	private final SongRepository songRepository;
	private final SecurityService securityService;

	public PlaylistService(PlaylistRepository playlistRepository,
							SongRepository songRepository,
							SecurityService securityService
							) {

		this.playlistRepository = playlistRepository;
		this.songRepository = songRepository;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public Page<Playlist> getAllPlaylists(Pageable pageable) {
	    User user = securityService.getCurrentUser();

	    return playlistRepository.findByOwnerId(user.getId(), pageable);
	}

	@Transactional(readOnly = true)
	public List<Song> getPlaylistById(String playlistId) {

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		if (playlist.getSongIds() == null || playlist.getSongIds().isEmpty()) {
			return new ArrayList<>();
		}

		List<Song> songs = new ArrayList<>();

		for (String songId : playlist.getSongIds()) {

			songRepository.findById(songId).ifPresent(songs::add);
		}

		return songs;
	}

	@Transactional
	public Playlist createPlaylist(Playlist playlist) {

		User user = securityService.getCurrentUser();
		playlist.setOwnerId(user.getId());

		return playlistRepository.save(playlist);
	}

	@Transactional
	public Playlist updatePlaylistById(Playlist playlist, String id) {

		Playlist existingPlaylist = playlistRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		existingPlaylist.setName(playlist.getName());

		return playlistRepository.save(existingPlaylist);
	}

	@Transactional
	public void deletePlaylistById(String id) {

		Playlist playlist = playlistRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		playlistRepository.delete(playlist);
	}

	@Transactional
	public void addSongToPlaylist(String songId, String playlistId) {

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		songRepository.findById(songId).orElseThrow(() -> new ResourceNotFoundException("Song not found"));

		if (playlist.getSongIds() == null) {
			playlist.setSongIds(new ArrayList<>());
		}
		
		if (playlist.getSongIds().contains(songId)) {
			throw new InvalidInputException("Song already exists in this playlist");
		}

		playlist.getSongIds().add(songId);

		playlistRepository.save(playlist);
	}

	@Transactional
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		if (playlist.getSongIds() != null) {
			playlist.getSongIds().remove(songId);
		}

		return playlistRepository.save(playlist);
	}

	@Transactional
	public Playlist moveSongToDesiredPosition(String playlistId, String songId, int position) {

		Playlist playlist = playlistRepository.findById(playlistId)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist not found"));

		songRepository.findById(songId)
				.orElseThrow(() -> new ResourceNotFoundException("Song not found"));

		if (position < 0 || position >= playlist.getSongIds().size()) {
			throw new InvalidInputException("Position is not valid");
		}

		int currPos = playlist.getSongIds().indexOf(songId);
		
		if (currPos == -1) {
			throw new ResourceNotFoundException("Song not present in playlist");
		}
		
		playlist.getSongIds().remove(currPos);
		playlist.getSongIds().add(position, songId);
		
		return playlistRepository.save(playlist);
	}

	@Transactional
	public Playlist insertNewSongAtPosition(String playlistId, String songId, String currentSongId) {
		
		Playlist playlist = playlistRepository
				.findById(playlistId)
				.orElseThrow(() -> new ResourceNotFoundException("Playlist Not Found"));
		
		songRepository.findById(songId)
				.orElseThrow(() -> new ResourceNotFoundException("Song Not Found"));
		
		if (playlist.getSongIds().contains(songId)) {
			throw new InvalidInputException("Song already present");
		}
		
		int positionCurrentSong = playlist.getSongIds().indexOf(currentSongId);
		
		if (positionCurrentSong == -1) {
			throw new InvalidInputException("Invalid Position");
		}
		
		playlist.getSongIds().add(positionCurrentSong + 1, songId);
		
		return playlistRepository.save(playlist);
	}
}
