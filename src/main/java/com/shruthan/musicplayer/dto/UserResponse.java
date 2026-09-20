package com.shruthan.musicplayer.dto;

import com.shruthan.musicplayer.model.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {

    private String id;
    private String userEmail;
    private Role role;
}