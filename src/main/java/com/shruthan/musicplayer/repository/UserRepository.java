package com.shruthan.musicplayer.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.shruthan.musicplayer.model.User;


public interface UserRepository extends MongoRepository<User, String> {

	public User findByUserEmail(String userEmail);
}
