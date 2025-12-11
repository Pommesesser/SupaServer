package org.me.supaserver;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class GameSessionManager {
    @Autowired
    private GameEngine gameEngine;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RedisTemplate<String, GameSession> redisTemplate;

    public String startSession() {
        String gameId = UUID.randomUUID().toString();
        GameSession initialGameSession = new GameSession(0, GameState.newGame(), System.currentTimeMillis());
        redisTemplate.opsForValue().set(gameId, initialGameSession);
        redisTemplate.expire(gameId, 30, TimeUnit.MINUTES);
        return gameId;
    }

    public String joinSession(String gameId) {
        GameSession session = redisTemplate.opsForValue().get(gameId);
        if (session == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        int player = switch (session.players()) {
            case 0 -> 1;
            case 1 -> -1;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session full");
        };

        GameSession newSession = new GameSession(session.players() + 1, session.gameState(), session.creationTime());
        redisTemplate.opsForValue().set(gameId, newSession);

        return jwtService.token(gameId, player);
    }

    public GameState getGameState(String gameId, String token) {
        if (!jwtService.gameId(token).equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        GameSession gameSession = redisTemplate.opsForValue().get(gameId);
        if (gameSession == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        return gameSession.gameState();
    }

    public void makeMove(String gameId, String token, Move move) {
        if (!jwtService.gameId(token).equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        GameSession session = redisTemplate.opsForValue().get(gameId);
        if (session == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        int playerNumber = jwtService.player(token);

        if (session.players() < 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game has not started yet");

        GameState newGameState = gameEngine.nextState(session.gameState(), move, playerNumber);
        if (session.gameState().equals(newGameState))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Illegal Move");

        redisTemplate.opsForValue().set(gameId, new GameSession(session.players(), newGameState, session.creationTime()));
    }

    public int getPlayerNumber(String gameId, String token) {
        if (!jwtService.gameId(token).equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return jwtService.player(token);
    }
}
