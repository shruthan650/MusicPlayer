package com.shruthan.musicplayer.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shruthan.musicplayer.model.Playlist;


public interface PlaylistRepository extends MongoRepository<Playlist, String>{

	public List<Playlist> findBySongIdsContaining(String songIds);
}
