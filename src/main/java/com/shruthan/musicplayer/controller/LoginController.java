package com.shruthan.musicplayer.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.model.Login;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.security.JWTService;
import com.shruthan.musicplayer.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public LoginController(
    		UserService userService,
    		AuthenticationManager authenticationManager,
    		JWTService jwtService) {
    	
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody User user) {

        User savedUser = userService.registerUser(user);

        UserResponse response = new UserResponse(
            savedUser.getId(),
            savedUser.getUserEmail(),
            savedUser.getRole()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    
    @PostMapping("/login")
    public String getCredentials(@RequestBody Login login) {

		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(login.getUserEmail(), login.getPassword()));
		
		return jwtService.generateToken(authentication.getName());
    }
}