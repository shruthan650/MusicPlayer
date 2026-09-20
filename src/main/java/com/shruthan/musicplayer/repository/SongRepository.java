package com.shruthan.musicplayer.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.shruthan.musicplayer.model.Song;

public interface SongRepository extends MongoRepository<Song, String>{

	List<Song> findByTitleContainingIgnoreCase(String title);

	List<Song> findByArtistNameContainingIgnoreCase(String artistName);

	List<Song> findByAlbumNameContainingIgnoreCase(String albumName);
}
