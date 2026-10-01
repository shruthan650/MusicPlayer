package com.shruthan.musicplayer.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shruthan.musicplayer.dto.LoginResponse;
import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.exception.ResourceNotFoundException;
import com.shruthan.musicplayer.model.Role;
import com.shruthan.musicplayer.model.Song;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.*;
import com.shruthan.musicplayer.security.CustomUserDetails;
import com.shruthan.musicplayer.security.JWTService;

@Service
public class UserService implements UserDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecurityService securityService;
	private final JWTService jwtService;
	private final PlaylistRepository playlistRepository;
	private final PlayHistoryRepository playHistoryRepository;
	private final SongRepository songRepository;
	private final SongService songService;

	public UserService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			SecurityService securityService,
			JWTService jwtService,
			SongRepository songRepository,
			PlayHistoryRepository playHistoryRepository,
			PlaylistRepository playlistRepository,
			SongService songService) {

		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.securityService = securityService;
		this.jwtService = jwtService;
		this.playlistRepository = playlistRepository;
		this.playHistoryRepository = playHistoryRepository;
		this.songRepository = songRepository;
		this.songService = songService;
	}

	public User loadUserByEmail(String userEmail) {

		logger.debug("Loading user by email");

		return userRepository.findByUserEmail(userEmail);
	}

	@Override
	public UserDetails loadUserByUsername(String username) {

		logger.debug("Loading user details for authentication");

		User user = userRepository.findByUserEmail(username);

		if (user == null) {
			logger.warn("User authentication failed: user not found");
			throw new UsernameNotFoundException("User not found: " + username);
		}

		return new CustomUserDetails(user);
	}

	@Transactional
	public User registerUser(User user) {

		logger.info("Registering new user with role: {}", user.getRole());

		if (user.getRole() == null) {
			logger.warn("Registration failed: role is missing");
			throw new InvalidInputException("Role is required");
		}

		if (user.getRole() == Role.ADMIN) {
			logger.warn("Registration attempt with ADMIN role rejected");
			throw new InvalidInputException("Admin registration is not allowed");
		}

		if (userRepository.findByUserEmail(user.getUserEmail()) != null) {
			logger.warn("Registration failed: email already registered");
			throw new InvalidInputException("Email already registered");
		}

		user.setPassword(passwordEncoder.encode(user.getPassword()));

		User savedUser = userRepository.save(user);

		logger.info("User registered successfully: {}", savedUser.getId());

		return savedUser;
	}

	@Transactional(readOnly = true)
	public UserResponse getCurrentUser() {

		User user = securityService.getCurrentUser();

		logger.debug("Fetching current user: {}", user.getId());

		return new UserResponse(user.getId(), user.getUserEmail(), user.getRole());
	}

	@Transactional
	public LoginResponse updateEmail(String email) {

		User user = securityService.getCurrentUser();

		logger.info("Updating email for user: {}", user.getId());

		User existing = userRepository.findByUserEmail(email);

		if (existing != null && !existing.getId().equals(user.getId())) {

			logger.warn("Email update rejected: email already registered");

			throw new InvalidInputException("Email already registered");
		}

		user.setUserEmail(email);

		User savedUser = userRepository.save(user);

		String token = jwtService.generateToken(savedUser.getUserEmail());

		UserResponse response = new UserResponse(savedUser.getId(), savedUser.getUserEmail(), savedUser.getRole());

		logger.info("Email updated successfully for user: {}", savedUser.getId());

		return new LoginResponse(token, response);
	}

	@Transactional
	public void updatePassword(String oldPassword, String newPassword) {

		User user = securityService.getCurrentUser();

		logger.info("Password update requested for user: {}", user.getId());

		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {

			logger.warn("Password update failed for user: {}", user.getId());

			throw new InvalidInputException("Incorrect current password");
		}

		user.setPassword(passwordEncoder.encode(newPassword));

		userRepository.save(user);

		logger.info("Password updated successfully for user: {}", user.getId());
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getAllUsers() {

		logger.debug("Fetching all users");

		List<UserResponse> users = userRepository.findAll().stream()
				.map(user -> new UserResponse(user.getId(), user.getUserEmail(), user.getRole())).toList();

		logger.debug("Fetched {} users", users.size());

		return users;
	}

	@Transactional
	public void deleteUser(String userId) throws IOException {

		User currentUser = securityService.getCurrentUser();

		logger.info("Admin requested deletion of user: {}", userId);

		if (currentUser.getId().equals(userId)) {

			logger.warn("Admin attempted to delete their own account: {}", userId);

			throw new InvalidInputException("Admin cannot delete their own account");
		}

		User user = userRepository.findById(userId).orElseThrow(() -> {
			logger.warn("User not found while deleting: {}", userId);

			return new ResourceNotFoundException("User not found");
		});

		playlistRepository.deleteByOwnerId(user.getId());

		if (user.getRole() == Role.ARTIST) {

			logger.info("Deleting songs owned by artist: {}", userId);

			List<Song> songs = songRepository.findByOwnerId(user.getId());

			for (Song song : songs) {
				songService.deleteSongById(song.getId());
			}
		}

		playHistoryRepository.deleteByUserId(user.getId());
		userRepository.delete(user);

		logger.info("User deleted successfully: {}", userId);
	}
}