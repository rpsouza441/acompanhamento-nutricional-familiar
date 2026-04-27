package com.nutritracker.config;

import com.nutritracker.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtProperties properties;
  private final SecretKey key;

  public JwtService(JwtProperties properties) {
    this.properties = properties;
    this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(Usuario usuario) {
    return generateToken(usuario, properties.expirationMs(), "access");
  }

  public String generateRefreshToken(Usuario usuario) {
    return generateToken(usuario, properties.refreshExpirationMs(), "refresh");
  }

  public String getSubject(String token) {
    return claims(token).getSubject();
  }

  public boolean isRefreshToken(String token) {
    return "refresh".equals(claims(token).get("token_type", String.class));
  }

  private String generateToken(Usuario usuario, long durationMs, String tokenType) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(usuario.getEmail())
        .claim("user_id", usuario.getId())
        .claim("role", usuario.getRole().name())
        .claim("token_type", tokenType)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(durationMs)))
        .signWith(key)
        .compact();
  }

  private Claims claims(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }
}
