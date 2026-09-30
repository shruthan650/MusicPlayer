package com.shruthan.musicplayer.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import com.shruthan.musicplayer.model.Playlist;

public interface PlaylistRepository extends MongoRepository<Playlist, String> {

    List<Playlist> findBySongIdsContaining(String songId);

    List<Playlist> deleteByOwnerId(String ownerId);

    List<Playlist> findByOwnerId(String id);

    @Query("{}")
    @Update("{ '$pull': { 'songIds': ?0 } }")
    void pullSongFromAllPlaylists(String songId);
}