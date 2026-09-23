package com.shruthan.musicplayer.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("songRepo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Song {

	@Id
	private String id;
	
	private String ownerId;
	
	@NotBlank
	private String title;
	
	@NotBlank
	private String artistName;

	private String albumName;
	
	private String genre;
	
	private Long duration;

	@NotBlank
	private String filePath;
}
