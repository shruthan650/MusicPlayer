package com.shruthan.musicplayer.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.Resource;
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

import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.service.SongService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class SongController {

	private final SongService service;

	public SongController(SongService service) {
		this.service = service;
	}

	@GetMapping("/songs")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public List<Song> getAllSongs() {
		return service.getAllSongs();
	}

	@PostMapping("/song")
	@PreAuthorize("hasAnyRole('ADMIN', 'ARTIST')")
	public Song addSong(@Valid @RequestBody Song song) {
		return service.addSong(song);
	}

	@GetMapping("/song/{songId}")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public Song getSongById(@PathVariable String songId) {
		return service.getSongById(songId);
	}

	@PutMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void updateSongById(@PathVariable String songId, @Valid @RequestBody Song song) {
		service.updateSongById(song, songId);
	}

	@DeleteMapping("/song/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void deleteSongById(@PathVariable String songId) throws IOException {
		service.deleteSongById(songId);
	}

	@PostMapping("/song/upload/{songId}")
	@PreAuthorize("hasRole('ADMIN') or @securityService.isSongOwner(#songId)")
	public void uploadSong(@RequestParam("songFile") MultipartFile songFile, @PathVariable String songId)
			throws IOException {
//		return "File Details {Name : " + songFile.getName() + "\n Size : " + songFile.getSize() + "\n Type : " + songFile.getContentType() + "}";

		service.uploadSong(songFile, songId);
	}

	@GetMapping("/song/{songId}/audio")
	@PreAuthorize("hasAnyRole('ADMIN', 'USER', 'ARTIST')")
	public ResponseEntity<StreamingResponseBody> streamSong(
	        @PathVariable String songId,
	        @RequestHeader(value = "Range", required = false) String range)
	        throws IOException {

	    Resource resource = service.streamSong(songId);

	    if (resource == null) {
	        return ResponseEntity
	                .status(HttpStatus.NOT_FOUND)
	                .build();
	    }

	    long contentLength = resource.contentLength();

	    String fileName = resource.getFilename().toLowerCase();

	    MediaType mediaType;

	    if (fileName.endsWith(".mp3")) {
	        mediaType = MediaType.parseMediaType("audio/mpeg");
	    } else if (fileName.endsWith(".wav")) {
	        mediaType = MediaType.parseMediaType("audio/wav");
	    } else {
	        return ResponseEntity
	                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
	                .build();
	    }

	    if (range == null) {

	        StreamingResponseBody body = outputStream -> {

	            try (InputStream inputStream = resource.getInputStream()) {
	                inputStream.transferTo(outputStream);
	            }

	        };

	        return ResponseEntity
	                .ok()
	                .contentType(mediaType)
	                .contentLength(contentLength)
	                .body(body);
	    }

	    HttpRange httpRange;

	    try {
	        httpRange = HttpRange.parseRanges(range).get(0);
	    } catch (IllegalArgumentException e) {

	        return ResponseEntity
	                .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
	                .build();
	    }

	    long start = httpRange.getRangeStart(contentLength);
	    long end = httpRange.getRangeEnd(contentLength);

	    if (start < 0 ||
	            end < start ||
	            start >= contentLength ||
	            end >= contentLength) {

	        return ResponseEntity
	                .status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
	                .build();
	    }

	    long count = end - start + 1;

	    StreamingResponseBody body = outputStream -> {

	        try (InputStream inputStream = resource.getInputStream()) {

	            long skipped = 0;

	            while (skipped < start) {

	                long currentSkip = inputStream.skip(start - skipped);

	                if (currentSkip == 0) {
	                    break;
	                }

	                skipped += currentSkip;
	            }

	            byte[] buffer = new byte[8192];

	            long remaining = count;

	            while (remaining > 0) {

	                int bytesToRead =
	                        (int) Math.min(buffer.length, remaining);

	                int bytesRead =
	                        inputStream.read(buffer, 0, bytesToRead);

	                if (bytesRead == -1) {
	                    break;
	                }

	                outputStream.write(buffer, 0, bytesRead);

	                remaining -= bytesRead;
	            }
	        }
	    };

	    return ResponseEntity
	            .status(HttpStatus.PARTIAL_CONTENT)
	            .header("Accept-Ranges", "bytes")
	            .header(
	                    "Content-Range",
	                    "bytes " + start + "-" + end + "/" + contentLength
	            )
	            .contentLength(count)
	            .contentType(mediaType)
	            .body(body);
	}
}