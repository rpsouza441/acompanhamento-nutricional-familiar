package com.nutritracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "nutritracker.jwt")
public record JwtProperties(String secret, long expirationMs, long refreshExpirationMs) {}
