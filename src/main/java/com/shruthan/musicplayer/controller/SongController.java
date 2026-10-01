package com.shruthan.musicplayer.controller;

import com.shruthan.musicplayer.dto.ArtistStatistics;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.SongService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

	private final SongService songService;

	public SongController(SongService songService) {
		this.songService = songService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<Page<Song>> getAllSongs(@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(songService.getAllSongs(pageable));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Song> getSongById(@PathVariable String id) {
		return ResponseEntity.ok(songService.getSongById(id));
	}

	@PostMapping()
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Song> addSong(@RequestBody Song song) {
		Song createdSong = songService.addSong(song);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdSong);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Void> updateSong(@PathVariable String id, @RequestBody Song song) {
		songService.updateSongById(song, id);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Void> deleteSong(@PathVariable String id) {
		songService.deleteSongById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upload")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Void> uploadSongFile(@PathVariable String id, @RequestParam("file") MultipartFile file) {
		songService.uploadSongFile(id, file);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/cover")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Song> uploadCoverImage(@PathVariable String id, @RequestParam("file") MultipartFile file) {
		Song updatedSong = songService.uploadCoverImage(id, file);
		return ResponseEntity.ok(updatedSong);
	}

	@GetMapping("/{id}/stream")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<ResourceRegion> streamAudio(@PathVariable String id, @RequestHeader HttpHeaders headers)
			throws IOException {

		Resource songResource = songService.getSongResource(id);
		long contentLength = songResource.contentLength();

		HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().get(0);

		MediaType mediaType = MediaTypeFactory.getMediaType(songResource).orElse(MediaType.APPLICATION_OCTET_STREAM);

		if (range != null) {
			long start = range.getRangeStart(contentLength);
			long end = range.getRangeEnd(contentLength);

			long chunkSize = Math.min(1024 * 1024L, end - start + 1);
			ResourceRegion region = new ResourceRegion(songResource, start, chunkSize);

			return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).contentType(mediaType).body(region);
		} else {
			long chunkSize = Math.min(1024 * 1024L, contentLength);
			ResourceRegion region = new ResourceRegion(songResource, 0, chunkSize);

			return ResponseEntity.status(HttpStatus.OK).contentType(mediaType).body(region);
		}
	}

	@GetMapping("/{id}/cover")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Resource> getCoverImage(@PathVariable String id) {
		Resource coverResource = songService.getCoverImageResource(id);
		MediaType mediaType = MediaTypeFactory.getMediaType(coverResource).orElse(MediaType.IMAGE_JPEG);

		return ResponseEntity.ok().contentType(mediaType).body(coverResource);
	}

	@PostMapping("/{id}/play")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#id)")
	public ResponseEntity<Void> incrementPlayCount(@PathVariable String id) {
		songService.incrementPlayCount(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/search")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<Page<Song>> searchSongs(@RequestParam("query") String query,
			@PageableDefault(size = 20) Pageable pageable) {
		return ResponseEntity.ok(songService.searchSongs(query, pageable));
	}

	@GetMapping("/most-played")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<Page<Song>> getMostPlayedSongs(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.getMostPlayedSongs(page, size));
	}

	@GetMapping("/most-played/genre")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<Page<Song>> getMostPlayedSongsByGenre(@RequestParam String genre,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.getMostPlayedSongsByGenre(genre, page, size));
	}

	@GetMapping("/artist/statistics")
	@PreAuthorize("hasRole('ARTIST')")
	public ResponseEntity<ArtistStatistics> getArtistStatistics() {
		return ResponseEntity.ok(songService.getArtistStatistics());
	}

	@GetMapping("/recommendations")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<List<Song>> getRecommendations() {
		return ResponseEntity.ok(songService.getRecommendations());
	}
}