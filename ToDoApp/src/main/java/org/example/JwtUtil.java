package org.example;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.vertx.ext.web.RoutingContext;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "your_secret_key_here";  // Use a secure key
    private static final long EXPIRATION_TIME = 1000 * 60 * 15; // 15 mins
    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 days

    // Generate Access Token
    public static String generateAccessToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // Generate Refresh Token
    public static String generateRefreshToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    // Extract email from Access Token
    public static String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Extract email from Refresh Token
    public static String extractEmailFromRefreshToken(String token) {
        return getClaims(token).getSubject();
    }

    // Validate token
    public static boolean validateAccessToken(String token) {
        try {
            getClaims(token); // will throw if invalid
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Used by middleware to extract email from context
    public static String extractEmail(RoutingContext ctx) {
        String authHeader = ctx.request().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.response().setStatusCode(401).end("Unauthorized: Missing or invalid token");
            return null;
        }

        String token = authHeader.substring(7);
        if (!validateAccessToken(token)) {
            ctx.response().setStatusCode(401).end("Unauthorized: Invalid or expired token");
            return null;
        }

        return extractEmail(token);
    }

    // Internal utility to parse claims
    private static Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET)
                .parseClaimsJws(token)
                .getBody();
    }
}
