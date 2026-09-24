package com.shruthan.musicplayer.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shruthan.musicplayer.dto.LoginResponse;
import com.shruthan.musicplayer.dto.PasswordUpdateRequest;
import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(
    		UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }

    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public LoginResponse updateEmail(@RequestParam String email) {
        return userService.updateEmail(email);
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(
            @RequestBody PasswordUpdateRequest request) {

        userService.updatePassword(
                request.getOldPassword(),
                request.getNewPassword());
    }
}