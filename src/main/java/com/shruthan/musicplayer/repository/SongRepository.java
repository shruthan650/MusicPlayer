package com.shruthan.musicplayer.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shruthan.musicplayer.model.Song;

public interface SongRepository extends MongoRepository<Song, String>{

}
