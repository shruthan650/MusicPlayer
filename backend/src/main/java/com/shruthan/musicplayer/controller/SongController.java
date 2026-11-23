package com.shruthan.musicplayer.controller;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.SongService;

@RestController
@RequestMapping("/api/songs")
public class SongController {

	private static final long CHUNK_SIZE = 1024 * 1024L; // 1 MB chunk streaming size

	private final SongService songService;

	public SongController(SongService songService) {
		this.songService = songService;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<Song>> getAllSongs(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.getAllSongs(PageRequest.of(page, size)));
	}

	@GetMapping("/{id}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Song> getSongById(@PathVariable String id) {
		return ResponseEntity.ok(songService.getSongById(id));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'ARTIST')")
	public ResponseEntity<Song> addSong(@RequestBody Song song) {
		return ResponseEntity.status(HttpStatus.CREATED).body(songService.addSong(song));
	}

	@PutMapping("/{id}")
	@PreAuthorize("@securityService.isSongOwner(#id) or hasRole('ADMIN')")
	public ResponseEntity<Void> updateSong(@PathVariable String id, @RequestBody Song song) {
		songService.updateSongById(song, id);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("@securityService.isSongOwner(#id) or hasRole('ADMIN')")
	public ResponseEntity<Void> deleteSong(@PathVariable String id) {
		songService.deleteSongById(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/upload")
	@PreAuthorize("@securityService.isSongOwner(#id) or hasRole('ADMIN')")
	public ResponseEntity<Void> uploadSong(@PathVariable String id, @RequestParam("file") MultipartFile file) {
		songService.uploadSongFile(id, file);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{id}/cover")
	@PreAuthorize("@securityService.isSongOwner(#id) or hasRole('ADMIN')")
	public ResponseEntity<Song> uploadCover(@PathVariable String id, @RequestParam("file") MultipartFile file) {
		return ResponseEntity.ok(songService.uploadCoverImage(id, file));
	}

	@GetMapping("/{id}/stream")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ResourceRegion> streamSong(@PathVariable String id,
			@RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader) throws IOException {

		Resource resource = songService.getSongResource(id);
		long contentLength = resource.contentLength();
		MediaType mediaType = resolveMediaType(resource);

		if (rangeHeader == null) {
			ResourceRegion region = new ResourceRegion(resource, 0, Math.min(CHUNK_SIZE, contentLength));
			return ResponseEntity.ok().contentType(mediaType).contentLength(contentLength).body(region);
		}

		HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
		long start = httpRange.getRangeStart(contentLength);
		long end = httpRange.getRangeEnd(contentLength);

		if (start >= contentLength) {
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
					.header(HttpHeaders.CONTENT_RANGE, "bytes */" + contentLength).build();
		}

		long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);

		ResourceRegion region = new ResourceRegion(resource, start, rangeLength);

		return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
				.header(HttpHeaders.CONTENT_RANGE,
						String.format("bytes %d-%d/%d", start, start + rangeLength - 1, contentLength))
				.contentType(mediaType).contentLength(rangeLength).body(region);
	}

	@GetMapping("/{id}/cover")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Resource> getCover(@PathVariable String id) {
		Resource resource = songService.getCoverImageResource(id);
		return ResponseEntity.ok().contentType(resolveMediaType(resource)).body(resource);
	}

	@PostMapping("/{id}/play")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> incrementPlayCount(@PathVariable String id) {
		songService.incrementPlayCount(id);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/search")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<Song>> searchSongs(@RequestParam String query,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.searchSongs(query, PageRequest.of(page, size)));
	}

	@GetMapping("/most-played")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<Song>> getMostPlayedSongs(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.getMostPlayedSongs(page, size));
	}

	@GetMapping("/most-played/genre")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<Song>> getMostPlayedByGenre(@RequestParam String genre,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		return ResponseEntity.ok(songService.getMostPlayedSongsByGenre(genre, page, size));
	}

	@GetMapping("/artist/statistics")
	@PreAuthorize("hasRole('ARTIST')")
	public ResponseEntity<?> getArtistStatistics() {
		return ResponseEntity.ok(songService.getArtistStatistics());
	}

	@GetMapping("/recommendations")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> getRecommendations() {
		return ResponseEntity.ok(songService.getRecommendations());
	}

	private MediaType resolveMediaType(Resource resource) {
		return MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM);
	}
}