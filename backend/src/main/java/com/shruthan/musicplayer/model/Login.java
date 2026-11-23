package com.shruthan.musicplayer.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document("loginRepo")
@AllArgsConstructor
@NoArgsConstructor
public class Login {

	@NotBlank
	private String userEmail;
	
	@NotBlank
	private String password;
}
