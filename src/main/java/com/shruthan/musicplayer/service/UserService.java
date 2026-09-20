package com.shruthan.musicplayer.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shruthan.musicplayer.exception.InvalidInputException;
import com.shruthan.musicplayer.model.Role;
import com.shruthan.musicplayer.model.User;
import com.shruthan.musicplayer.repository.UserRepository;
import com.shruthan.musicplayer.security.CustomUserDetails;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User loadUserByEmail(String userEmail) {
        return userRepository.findByUserEmail(userEmail);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = userRepository.findByUserEmail(username);

        if (user == null) {
            throw new UsernameNotFoundException(
                "User not found: " + username
            );
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

        user.setPassword(
            passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }
}