package com.shruthan.musicplayer.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Login {

	@NotBlank
	private String userEmail;
	
	@NotBlank
	private String password;
}
