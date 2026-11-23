package com.shruthan.musicplayer.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shruthan.musicplayer.dto.LoginResponse;
import com.shruthan.musicplayer.dto.PasswordUpdateRequest;
import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/admin/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<List<UserResponse>> getAllUsers() {
		return ResponseEntity.ok(userService.getAllUsers());
	}

	@DeleteMapping("/admin/users/{userId}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<Void> deleteUser(@PathVariable String userId) throws IOException {

		userService.deleteUser(userId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UserResponse> getCurrentUser() {
		return ResponseEntity.ok(userService.getCurrentUser());
	}

	@PutMapping("/email")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<LoginResponse> updateEmail(@RequestParam String email) {

		return ResponseEntity.ok(userService.updateEmail(email));
	}

	@PutMapping("/password")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateRequest request) {

		userService.updatePassword(request.getOldPassword(), request.getNewPassword());

		return ResponseEntity.ok().build();
	}
}