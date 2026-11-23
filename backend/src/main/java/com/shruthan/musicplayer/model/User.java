package com.shruthan.musicplayer.model;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("userRepo")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

	@Id
	private String id;
	
	@NotBlank
	private String userEmail;
	
	@NotBlank
	private String password;
	
	@NotNull
	private Role role;
	
	private Set<String> likedSongIds = new HashSet<>();
}
