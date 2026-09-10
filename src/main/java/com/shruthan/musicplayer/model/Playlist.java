package com.shruthan.musicplayer.model;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("Playlist")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Playlist {

	@Id
	private String id;
	
	@NotBlank
	private String name;
	
	private Set<String> songIds;
}
