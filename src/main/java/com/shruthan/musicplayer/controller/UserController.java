package com.shruthan.musicplayer.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shruthan.musicplayer.dto.PasswordUpdateRequest;
import com.shruthan.musicplayer.dto.UserResponse;
import com.shruthan.musicplayer.service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse getCurrentUser() {
        return service.getCurrentUser();
    }

    @PutMapping("/email")
    @PreAuthorize("isAuthenticated()")
    public UserResponse updateEmail(@RequestParam String email) {
        return service.updateEmail(email);
    }

    @PutMapping("/password")
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(
            @RequestBody PasswordUpdateRequest request) {

        service.updatePassword(
                request.getOldPassword(),
                request.getNewPassword()
        );
    }
}