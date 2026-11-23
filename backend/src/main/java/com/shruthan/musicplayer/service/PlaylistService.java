package com.shruthan.musicplayer.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(PlaylistService.class);

	private final PlaylistRepository playlistRepository;
	private final SongRepository songRepository;
	private final SecurityService securityService;

	public PlaylistService(
			PlaylistRepository playlistRepository,
			SongRepository songRepository,
			SecurityService securityService) {

		this.playlistRepository = playlistRepository;
		this.songRepository = songRepository;
		this.securityService = securityService;
	}

	@Transactional(readOnly = true)
	public Page<Playlist> getAllPlaylists(Pageable pageable) {

		User user = securityService.getCurrentUser();

		logger.debug("Fetching playlists for user: {}, page: {}, size: {}", user.getId(), pageable.getPageNumber(),
				pageable.getPageSize());

		Page<Playlist> playlists = playlistRepository.findByOwnerId(user.getId(), pageable);

		logger.debug("Fetched {} playlists for user: {}", playlists.getNumberOfElements(), user.getId());

		return playlists;
	}

	@Transactional(readOnly = true)
	public List<Song> getPlaylistById(String playlistId) {

		logger.debug("Fetching songs from playlist: {}", playlistId);

		Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> {
			logger.warn("Playlist not found: {}", playlistId);
			return new ResourceNotFoundException("Playlist not found");
		});

		if (playlist.getSongIds() == null || playlist.getSongIds().isEmpty()) {
			logger.debug("Playlist {} contains no songs", playlistId);
			return new ArrayList<>();
		}

		List<Song> songs = new ArrayList<>();

		for (String songId : playlist.getSongIds()) {
			songRepository.findById(songId).ifPresent(songs::add);
		}

		logger.debug("Fetched {} songs from playlist {}", songs.size(), playlistId);

		return songs;
	}

	@Transactional
	public Playlist createPlaylist(Playlist playlist) {

		User user = securityService.getCurrentUser();

		playlist.setOwnerId(user.getId());

		Playlist savedPlaylist = playlistRepository.save(playlist);

		logger.info("Playlist created successfully: {} by user: {}", savedPlaylist.getId(), user.getId());

		return savedPlaylist;
	}

	@Transactional
	public Playlist updatePlaylistById(Playlist playlist, String id) {

		logger.info("Updating playlist: {}", id);

		Playlist existingPlaylist = playlistRepository.findById(id).orElseThrow(() -> {
			logger.warn("Playlist not found while updating: {}", id);
			return new ResourceNotFoundException("Playlist not found");
		});

		existingPlaylist.setName(playlist.getName());

		Playlist updatedPlaylist = playlistRepository.save(existingPlaylist);

		logger.info("Playlist updated successfully: {}", id);

		return updatedPlaylist;
	}

	@Transactional
	public void deletePlaylistById(String id) {

		logger.info("Deleting playlist: {}", id);

		Playlist playlist = playlistRepository.findById(id).orElseThrow(() -> {
			logger.warn("Playlist not found while deleting: {}", id);
			return new ResourceNotFoundException("Playlist not found");
		});

		playlistRepository.delete(playlist);

		logger.info("Playlist deleted successfully: {}", id);
	}

	@Transactional
	public void addSongToPlaylist(String songId, String playlistId) {

		logger.info("Adding song {} to playlist {}", songId, playlistId);

		Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> {
			logger.warn("Playlist not found: {}", playlistId);
			return new ResourceNotFoundException("Playlist not found");
		});

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found: {}", songId);
			return new ResourceNotFoundException("Song not found");
		});

		if (playlist.getSongIds() == null) {
			playlist.setSongIds(new ArrayList<>());
		}

		if (playlist.getSongIds().contains(songId)) {
			logger.warn("Song {} already exists in playlist {}", songId, playlistId);

			throw new InvalidInputException("Song already exists in this playlist");
		}

		playlist.getSongIds().add(songId);

		playlistRepository.save(playlist);

		logger.info("Song {} added successfully to playlist {}", songId, playlistId);
	}

	@Transactional
	public Playlist removeSongFromPlaylist(String songId, String playlistId) {

		logger.info("Removing song {} from playlist {}", songId, playlistId);

		Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> {
			logger.warn("Playlist not found: {}", playlistId);
			return new ResourceNotFoundException("Playlist not found");
		});

		if (playlist.getSongIds() != null) {
			playlist.getSongIds().remove(songId);
		}

		Playlist updatedPlaylist = playlistRepository.save(playlist);

		logger.info("Song {} removed from playlist {}", songId, playlistId);

		return updatedPlaylist;
	}

	@Transactional
	public Playlist moveSongToDesiredPosition(String playlistId, String songId, int position) {

		logger.info("Moving song {} to position {} in playlist {}", songId, position, playlistId);

		Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> {
			logger.warn("Playlist not found: {}", playlistId);
			return new ResourceNotFoundException("Playlist not found");
		});

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found: {}", songId);
			return new ResourceNotFoundException("Song not found");
		});

		if (position < 0 || position >= playlist.getSongIds().size()) {
			logger.warn("Invalid position {} for playlist {}", position, playlistId);

			throw new InvalidInputException("Position is not valid");
		}

		int currPos = playlist.getSongIds().indexOf(songId);

		if (currPos == -1) {
			logger.warn("Song {} is not present in playlist {}", songId, playlistId);

			throw new ResourceNotFoundException("Song not present in playlist");
		}

		playlist.getSongIds().remove(currPos);
		playlist.getSongIds().add(position, songId);

		Playlist updatedPlaylist = playlistRepository.save(playlist);

		logger.info("Song {} moved to position {} in playlist {}", songId, position, playlistId);

		return updatedPlaylist;
	}

	@Transactional
	public Playlist insertNewSongAtPosition(String playlistId, String songId, String currentSongId) {

		logger.info("Inserting song {} after song {} in playlist {}", songId, currentSongId, playlistId);

		Playlist playlist = playlistRepository.findById(playlistId).orElseThrow(() -> {
			logger.warn("Playlist not found: {}", playlistId);
			return new ResourceNotFoundException("Playlist Not Found");
		});

		songRepository.findById(songId).orElseThrow(() -> {
			logger.warn("Song not found: {}", songId);
			return new ResourceNotFoundException("Song Not Found");
		});

		if (playlist.getSongIds().contains(songId)) {
			logger.warn("Song {} already exists in playlist {}", songId, playlistId);

			throw new InvalidInputException("Song already present");
		}

		int positionCurrentSong = playlist.getSongIds().indexOf(currentSongId);

		if (positionCurrentSong == -1) {
			logger.warn("Current song {} not found in playlist {}", currentSongId, playlistId);

			throw new InvalidInputException("Invalid Position");
		}

		playlist.getSongIds().add(positionCurrentSong + 1, songId);

		Playlist updatedPlaylist = playlistRepository.save(playlist);

		logger.info("Song {} inserted successfully into playlist {}", songId, playlistId);

		return updatedPlaylist;
	}
}