package com.shruthan.musicplayer.repository;

import com.shruthan.musicplayer.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongRepository extends MongoRepository<Song, String> {

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

	Page<Song> findByGenre(String genre, Pageable pageable);

	List<Song> findByOwnerId(String ownerId);

	void deleteByOwnerId(String ownerId);

	List<Song> findByGenreInOrderByPlayCountDesc(List<String> genres);

	Page<Song> findAllByOrderByPlayCountDesc(Pageable pageable);
}