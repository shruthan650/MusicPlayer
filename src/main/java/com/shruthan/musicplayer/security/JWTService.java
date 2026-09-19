package com.shruthan.musicplayer.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JWTService {

	@Value("${jwt_secret}")
	private String secret;
	private SecretKey sk;
	
	@PostConstruct
	public void init() {
		byte[] decoded = Decoders.BASE64.decode(secret);
		sk = Keys.hmacShaKeyFor(decoded);
	}

	public String generateToken(String name) {
		return Jwts
				.builder()
				.subject(name)
				.issuedAt(new Date(System.currentTimeMillis()))
				.expiration(new Date(System.currentTimeMillis() + 10 * 60 * 60 * 1000))
				.signWith(sk)
				.compact();
	}

	public String extractUserName(String token) {
		
		return Jwts
				.parser()
				.verifyWith(sk)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
	}
}
