package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Playlist;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlayHistoryRepository;
import com.shruthan.musicplayer.repository.PlaylistRepository;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;

@Service
public class SongService {

	private final SongRepository songRepository;
	private final PlaylistRepository playlistRepository;
	private final UserRepository userRepository;
	private final SecurityService securityService;
	private final PlayHistoryRepository playHistoryRepository;

	SongService(SongRepository repository, PlaylistRepository playlistRepository, UserRepository userRepository,
			SecurityService securityService, PlayHistoryRepository playHistoryRepository) {

		this.songRepository = repository;
		this.playlistRepository = playlistRepository;
		this.securityService = securityService;
		this.userRepository = userRepository;
		this.playHistoryRepository = playHistoryRepository;
	}

	public org.springframework.data.domain.Page<Song> getAllSongs(org.springframework.data.domain.Pageable pageable) {

		return songRepository.findAll(pageable);
	}

	public Song addSong(Song song) {

		User user = securityService.getCurrentUser();
		song.setOwnerId(user.getId());

		return songRepository.save(song);
	}

	public Song getSongById(String songId) {
		Song song = songRepository.findById(songId).orElse(null);

		if (song == null) {
			throw new ResourceNotFoundException(songId + " is not present");
		}

		return song;
	}

	public void updateSongById(Song song, String songId) {

		Song songInRepo = songRepository.findById(songId).orElse(null);

		if (songInRepo != null) {

			songInRepo.setAlbumName(song.getAlbumName());
			songInRepo.setArtistName(song.getArtistName());
			songInRepo.setDuration(song.getDuration());
			songInRepo.setGenre(song.getGenre());
			songInRepo.setTitle(song.getTitle());
			songRepository.save(song);

		} else {
			throw new ResourceNotFoundException("Song not found");
		}
	}

	public void deleteSongById(String songId) throws IOException {
		Song song = songRepository.findById(songId).orElse(null);

		if (song != null) {
			if (song.getFilePath() != null) {
				Files.deleteIfExists(Paths.get(song.getFilePath()));
			}

			songRepository.deleteById(songId);
			playHistoryRepository.deleteBySongId(songId);
			
			
			List<Playlist> playlistsContainingSongId = playlistRepository.findBySongIdsContaining(songId);

			for (Playlist playlist : playlistsContainingSongId) {
				playlist.getSongIds().remove(songId);
				playlistRepository.save(playlist);
			}
			
			List<User> users = userRepository.findByLikedSongIdsContaining(songId);

			for (User user : users) {
			    user.getLikedSongIds().remove(songId);
			    userRepository.save(user);
			}
			
		} else {
			throw new ResourceNotFoundException("Song not found");
		}
	}

	public void uploadSong(MultipartFile songFile, String songId)
			throws IOException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException {

		Song song = songRepository.findById(songId).orElseThrow(() -> new ResourceNotFoundException("Song not found"));

		String oldPath = song.getFilePath();

		String originalFileName = songFile.getOriginalFilename();

		if (songFile.isEmpty() || originalFileName == null || originalFileName.isBlank()) {

			throw new InvalidInputException("Invalid audio file");
		}

		String extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1).toLowerCase();

		String contentType = songFile.getContentType();

		if (!extension.equals("wav") && !extension.equals("mp3")) {
			throw new InvalidInputException("Only WAV and MP3 files are supported");
		}

		if (extension.equals("wav") && !"audio/wav".equalsIgnoreCase(contentType)) {

			throw new InvalidInputException("Invalid WAV file type");
		}

		if (extension.equals("mp3") && !"audio/mpeg".equalsIgnoreCase(contentType)) {

			throw new InvalidInputException("Invalid MP3 file type");
		}

		Path folder = Paths.get("songs");

		Files.createDirectories(folder);

		String fileName = UUID.randomUUID() + "." + extension;

		Path filePath = folder.resolve(fileName);

		Files.write(filePath, songFile.getBytes());

		// Read audio duration
		AudioFile audioFile = AudioFileIO.read(filePath.toFile());

		AudioHeader audioHeader = audioFile.getAudioHeader();

		int durationInSeconds = audioHeader.getTrackLength();

		long durationInMilliseconds = durationInSeconds * 1000L;

		song.setDuration(durationInMilliseconds);

		song.setFilePath(filePath.toString());

		songRepository.save(song);

		// Delete old audio file
		if (oldPath != null) {
			Files.deleteIfExists(Paths.get(oldPath));
		}
	}

	public Resource streamSong(String songId) throws IOException {

		Song song = songRepository.findById(songId).orElse(null);

		if (song != null && song.getFilePath() != null) {

			if (Files.exists(Path.of(song.getFilePath()))) {

				Path filePath = Paths.get(song.getFilePath());
				return new FileSystemResource(filePath);
			}

		}

		throw new ResourceNotFoundException("Song not found");
	}
	
	public MediaType getMediaType(Resource resource) {

	    String fileName = resource.getFilename().toLowerCase();

	    if (fileName.endsWith(".mp3")) {
	        return MediaType.parseMediaType("audio/mpeg");
	    }

	    if (fileName.endsWith(".wav")) {
	        return MediaType.parseMediaType("audio/wav");
	    }

	    return null;
	}
	
	public ResponseEntity<StreamingResponseBody> streamFullFile(
	        Resource resource,
	        MediaType mediaType,
	        long contentLength) {

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
	
	public HttpRange parseRange(String range) {

	    try {
	        return HttpRange.parseRanges(range).get(0);
	    } catch (IllegalArgumentException e) {
	        return null;
	    }
	}
	
	public boolean isValidRange(
	        long start,
	        long end,
	        long contentLength) {

	    return start >= 0
	            && end >= start
	            && start < contentLength
	            && end < contentLength;
	}
	
	public ResponseEntity<StreamingResponseBody> streamRange(
	        Resource resource,
	        MediaType mediaType,
	        long start,
	        long end,
	        long contentLength) {

	    long count = end - start + 1;

	    StreamingResponseBody body = outputStream -> {

	        try (InputStream inputStream = resource.getInputStream()) {

	            skipBytes(inputStream, start);

	            streamBytes(
	                    inputStream,
	                    outputStream,
	                    count
	            );
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
	
	private void skipBytes(
	        InputStream inputStream,
	        long start)
	        throws IOException {

	    long skipped = 0;

	    while (skipped < start) {

	        long currentSkip =
	                inputStream.skip(start - skipped);

	        if (currentSkip == 0) {
	            break;
	        }

	        skipped += currentSkip;
	    }
	}
	
	private void streamBytes(
	        InputStream inputStream,
	        OutputStream outputStream,
	        long count)
	        throws IOException {

	    byte[] buffer = new byte[8192];

	    long remaining = count;

	    while (remaining > 0) {

	        int bytesToRead =
	                (int) Math.min(buffer.length, remaining);

	        int bytesRead =
	                inputStream.read(
	                        buffer,
	                        0,
	                        bytesToRead
	                );

	        if (bytesRead == -1) {
	            break;
	        }

	        outputStream.write(
	                buffer,
	                0,
	                bytesRead
	        );

	        remaining -= bytesRead;
	    }
	}

	public Page<Song> searchSongs(String query, Pageable pageable) {
		return songRepository.searchSongs(query, pageable);
	}
	
	public void songCountTracker(String songId) {
		
		Song song = songRepository
		.findById(songId)
		.orElseThrow(() -> new ResourceNotFoundException(songId));
		
		song.setPlayCount(song.getPlayCount() + 1);
	}
	
	public Page<Song> getMostPlayedSongs(int page, int size) {

		Pageable pageable = PageRequest.of(
		        page,
		        size,
		        Sort.by(
		                Sort.Order.desc("playCount"),
		                Sort.Order.asc("title")
		        )
		);

	    return songRepository.findAllByOrderByPlayCountDesc(pageable);
	}
	
	public Page<Song> getMostPlayedSongsByGenre(
	        String genre,
	        int page,
	        int size) {

	    Pageable pageable = PageRequest.of(
	            page,
	            size,
	            Sort.by(
	                    Sort.Order.desc("playCount"),
	                    Sort.Order.asc("title")
	            )
	    );

	    return songRepository.findByGenreOrderByPlayCountDesc(
	            genre,
	            pageable
	    );
	}
}
