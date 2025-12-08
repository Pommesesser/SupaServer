package org.me.supaserver;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameSessionManager {
    @Autowired
    private GameEngine gameEngine;
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();
    private final SecretKey jwtKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public boolean serverIsFull() {
        int MAX_SESSIONS = 1_000;
        return activeSessions.size() >= MAX_SESSIONS;
    }

    public String startSession() {
        String gameId = UUID.randomUUID().toString();
        GameSession initialGameSession = new GameSession(0, GameState.newGame(), System.currentTimeMillis());
        activeSessions.put(gameId, initialGameSession);

        return gameId;
    }

    public GameState getGameState(String gameId, String token) {
        GameSession gameSession = activeSessions.get(gameId);
        if (gameSession == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tokenGameId = claims.get("gameId", String.class);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return gameSession.gameState();
    }

    public String joinSession(String gameId) {
        AtomicInteger playerNumberHolder = new AtomicInteger();
        GameSession newGameSession = activeSessions.computeIfPresent(gameId, (_, gameSession) -> {
            playerNumberHolder.set(switch (gameSession.players()) {
                case 0 -> 1;
                case 1 -> -1;
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session full");
            });

            return new GameSession(gameSession.players() + 1, gameSession.gameState(), gameSession.creationTime());
        });

        if (newGameSession == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        return Jwts.builder()
                .claim("gameId", gameId)
                .claim("playerNumber", playerNumberHolder.get())
                .signWith(jwtKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public void makeMove(String gameId, String token, Move move) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tokenGameId = claims.get("gameId", String.class);
        int playerNumber = claims.get("playerNumber", Integer.class);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        activeSessions.computeIfPresent(gameId, (_, gameSession) -> {
            if (gameSession.players() < 2)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has not started yet");

            GameState newGameState = gameEngine.nextState(gameSession.gameState(), move, playerNumber);

            if (gameSession.gameState().equals(newGameState))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal Move");

            return new GameSession(gameSession.players(), newGameState, gameSession.creationTime());
        });
    }

    public int getPlayerNumber(String gameId, String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(jwtKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        String tokenGameId = claims.get("gameId", String.class);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return claims.get("playerNumber", Integer.class);
    }
}
