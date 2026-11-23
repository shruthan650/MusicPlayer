package com.shruthan.musicplayer.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.shruthan.musicplayer.model.PlayHistory;

public interface PlayHistoryRepository extends MongoRepository<PlayHistory, String> {

	Page<PlayHistory> findByUserIdOrderByPlayedAtDesc(
	        String userId,
	        Pageable pageable
	);
	
	Optional<PlayHistory> findByUserIdAndSongId(
            String userId,
            String songId);
	
	void deleteBySongId(String songId);
	
	void deleteByUserId(String userId);
}
