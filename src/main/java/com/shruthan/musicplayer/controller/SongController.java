package com.shruthan.musicplayer.controller;

import java.io.IOException;
import java.util.List;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.shruthan.musicplayer.dto.ArtistStatistics;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.PlayHistoryService;
import com.shruthan.musicplayer.service.SongService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SongController {

	private final SongService songService;
	private final PlayHistoryService playHistoryService;

	public SongController(SongService service, PlayHistoryService playHistoryService) {
		this.songService = service;
		this.playHistoryService = playHistoryService;
	}

	@GetMapping("/songs")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Page<Song> getAllSongs(@PageableDefault(size = 10, page = 0) Pageable pageable) {
		return songService.getAllSongs(pageable);
	}

	@PostMapping("/song")
	@PreAuthorize("hasAnyRole('ADMIN', 'ARTIST')")
	public Song addSong(@Valid @RequestBody Song song) {
		return songService.addSong(song);
	}

	@GetMapping("/song/{songId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Song getSongById(@PathVariable String songId) {
		return songService.getSongById(songId);
	}

	@PutMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void updateSongById(@PathVariable String songId, @Valid @RequestBody Song song) {
		songService.updateSongById(song, songId);
	}

	@DeleteMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void deleteSongById(@PathVariable String songId) throws IOException {
		songService.deleteSongById(songId);
	}

	@PostMapping("/song/upload/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void uploadSong(@RequestParam("songFile") MultipartFile songFile, @PathVariable String songId)
			throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {

		songService.uploadSong(songFile, songId);
	}

	@PostMapping("/song/{songId}/cover")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public Song uploadCover(@PathVariable String songId, @RequestParam("coverFile") MultipartFile coverFile)
			throws IOException {

		return songService.uploadCover(songId, coverFile);
	}

	@GetMapping("/songs/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Page<Song> searchSongs(@RequestParam String q, @PageableDefault(size = 10, page = 0) Pageable pageable) {

		return songService.searchSongs(q, pageable);
	}

	@GetMapping("/song/{songId}/audio")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<StreamingResponseBody> streamSong(@PathVariable String songId,
			@RequestHeader(value = "Range", required = false) String range) throws IOException {

		Resource resource = songService.streamSong(songId);

		if (resource == null) {
			return ResponseEntity.notFound().build();
		}

		// Record playback
		playHistoryService.addToHistory(songId);
		songService.songCountTracker(songId);

		long contentLength = resource.contentLength();

		MediaType mediaType = songService.getMediaType(resource);

		if (mediaType == null) {
			return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
		}

		// Normal request
		if (range == null) {
			return songService.streamFullFile(resource, mediaType, contentLength);
		}

		// Range request
		HttpRange httpRange = songService.parseRange(range);

		if (httpRange == null) {
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
		}

		long start = httpRange.getRangeStart(contentLength);
		long end = httpRange.getRangeEnd(contentLength);

		if (!songService.isValidRange(start, end, contentLength)) {
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE).build();
		}

		return songService.streamRange(resource, mediaType, start, end, contentLength);
	}

	@GetMapping("/songs/most-played")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Page<Song> getMostPlayedSongs(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return songService.getMostPlayedSongs(page, size);
	}

	@GetMapping("/songs/most-played/genre/{genre}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Page<Song> getMostPlayedSongsByGenre(@PathVariable String genre, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		return songService.getMostPlayedSongsByGenre(genre, page, size);
	}

	@GetMapping("/artist/statistics")
	@PreAuthorize("hasRole('ARTIST')")
	public ArtistStatistics getArtistStatistics() {
		return songService.getArtistStatistics();
	}

	@GetMapping("/songs/recommend")
	@PreAuthorize("isAuthenticated()")
	public List<Song> getRecommendations() {
		return songService.getRecommendations();
	}
}