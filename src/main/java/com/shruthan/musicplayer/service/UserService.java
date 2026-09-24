package com.shruthan.musicplayer.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.dto.LoginResponse;
import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.model.Role;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.PlayHistoryRepository;
import com.shruthan.musicplayer.repository.SongRepository;
import com.shruthan.musicplayer.repository.UserRepository;
import com.shruthan.musicplayer.security.CustomUserDetails;
import com.shruthan.musicplayer.security.JWTService;

@Service
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityService securityService;
	private final JWTService jwtService;
	private final SongRepository songRepository;
	private final PlayHistoryRepository playHistoryRepository;

	public UserService(
			UserRepository userRepository, 
			PasswordEncoder passwordEncoder, 
			SecurityService securityService,
			JWTService jwtService, 
			SongRepository songRepository, 
			PlayHistoryRepository playHistoryRepository
			) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityService = securityService;
		this.jwtService = jwtService;
		this.songRepository = songRepository;
		this.playHistoryRepository = playHistoryRepository;
	}

	public User loadUserByEmail(String userEmail) {
		return userRepository.findByUserEmail(userEmail);
	}

	@Override
	public UserDetails loadUserByUsername(String username) {

		User user = userRepository.findByUserEmail(username);

		if (user == null) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		return new CustomUserDetails(user);
	}

	public User registerUser(User user) {

		if (user.getRole() == null) {
			throw new InvalidInputException("Role is required");
		}

		if (user.getRole() == Role.ADMIN) {
			throw new InvalidInputException("Admin registration is not allowed");
		}

		if (userRepository.findByUserEmail(user.getUserEmail()) != null) {
			throw new InvalidInputException("Email already registered");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		return userRepository.save(user);
	}

	public UserResponse getCurrentUser() {

		User user = securityService.getCurrentUser();

		return new UserResponse(user.getId(), user.getUserEmail(), user.getRole());
	}

	public LoginResponse updateEmail(String email) {

		User user = securityService.getCurrentUser();

		User existing = userRepository.findByUserEmail(email);

		if (existing != null && !existing.getId().equals(user.getId())) {
			throw new InvalidInputException("Email already registered");
		}

		user.setUserEmail(email);

		User savedUser = userRepository.save(user);

		String token = jwtService.generateToken(savedUser.getUserEmail());

		UserResponse response = new UserResponse(savedUser.getId(), savedUser.getUserEmail(), savedUser.getRole());

		return new LoginResponse(token, response);
	}

	public void updatePassword(String oldPassword, String newPassword) {

	    User user = securityService.getCurrentUser();

	    if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
	        throw new InvalidInputException("Incorrect current password");
	    }

	    user.setPassword(passwordEncoder.encode(newPassword));

	    userRepository.save(user);
	}
}