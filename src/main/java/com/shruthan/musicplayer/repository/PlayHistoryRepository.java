package com.shruthan.musicplayer.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shruthan.musicplayer.model.PlayHistory;

public interface PlayHistoryRepository extends MongoRepository<PlayHistory, String> {

	List<PlayHistory> findByUserIdOrderByPlayedAtDesc(String userId);
	
	Optional<PlayHistory> findByUserIdAndSongId(
            String userId,
            String songId);
	
	void deleteBySongId(String songId);
}
