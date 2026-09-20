package com.shruthan.musicplayer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.shruthan.musicplayer.model.Song;

public interface SongRepository extends MongoRepository<Song, String>{
	@Query("""
			{
			    '$or': [
			        { 'title': { '$regex': ?0, '$options': 'i' } },
			        { 'artistName': { '$regex': ?0, '$options': 'i' } },
			        { 'albumName': { '$regex': ?0, '$options': 'i' } }
			    ]
			}
			""")
			Page<Song> searchSongs(String query, Pageable pageable);
}
