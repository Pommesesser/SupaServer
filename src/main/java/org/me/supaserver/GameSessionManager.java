package org.me.supaserver;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class GameSessionManager {
    @Autowired
    private GameEngine gameEngine;
    @Autowired
    private JwtService jwtService;
    private final Map<String, GameSession> activeSessions = new ConcurrentHashMap<>();

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

        return jwtService.token(gameId, playerNumberHolder.get());
    }

    public GameState getGameState(String gameId, String token) {
        GameSession gameSession = activeSessions.get(gameId);
        if (gameSession == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Session does not exist");

        String tokenGameId = jwtService.gameId(token);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return gameSession.gameState();
    }

    public void makeMove(String gameId, String token, Move move) {
        String tokenGameId = jwtService.gameId(token);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        int playerNumber = jwtService.player(token);

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
        String tokenGameId = jwtService.gameId(token);
        if (!tokenGameId.equals(gameId))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return jwtService.player(token);
    }
}
