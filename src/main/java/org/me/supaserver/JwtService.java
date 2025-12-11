package org.me.supaserver;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
class JwtService {
    private final SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private final long DEFAULT_TTL = 1800000;

    public String token(String gameId, int player) {
        long time = System.currentTimeMillis();
        Date now = new Date(time);
        Date exp = new Date(time + DEFAULT_TTL);

        return Jwts.builder()
                .claim("gameId", gameId)
                .claim("player", player)
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String gameId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("gameId", String.class);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }

    public int player(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("player", Integer.class);
        } catch (JwtException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
    }
}