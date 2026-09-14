package com.shruthan.musicplayer.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	@PostMapping("/song/upload/{songId}")
	public void uploadSong(@RequestParam("songFile") MultipartFile songFile, @PathVariable String songId)
			throws IOException {
//		return "File Details {Name : " + songFile.getName() + "\n Size : " + songFile.getSize() + "\n Type : " + songFile.getContentType() + "}";

		service.uploadSong(songFile, songId);
	}

	@GetMapping("/song/{songId}/audio")
	public ResponseEntity<StreamingResponseBody> streamSong(@PathVariable String songId,
			@RequestHeader(value = "Range", required = false) String range) throws IOException {

		Resource resource = service.streamSong(songId);

		long contentLength = resource.contentLength();

		if (range == null) {

			StreamingResponseBody body = outputStream -> {
				try (InputStream inputStream = resource.getInputStream()) {
					inputStream.transferTo(outputStream);
				}
			};

			return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/wav")).contentLength(contentLength)
					.body(body);
		}

		HttpRange httpRange = HttpRange.parseRanges(range).get(0);

		long start = httpRange.getRangeStart(contentLength);
		long end = httpRange.getRangeEnd(contentLength);

		long count = end - start + 1;

		StreamingResponseBody body = outputStream -> {

			try (InputStream inputStream = resource.getInputStream()) {

				inputStream.skip(start);

				byte[] buffer = new byte[8192];

				long remaining = count;

				while (remaining > 0) {

					int bytesToRead = (int) Math.min(buffer.length, remaining);

					int bytesRead = inputStream.read(buffer, 0, bytesToRead);

					if (bytesRead == -1) {
						break;
					}

					outputStream.write(buffer, 0, bytesRead);

					remaining -= bytesRead;
				}
			}
		};

		return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).header("Accept-Ranges", "bytes")
				.header("Content-Range", "bytes " + start + "-" + end + "/" + contentLength).contentLength(count)
				.contentType(MediaType.parseMediaType("audio/wav")).body(body);
	}
}