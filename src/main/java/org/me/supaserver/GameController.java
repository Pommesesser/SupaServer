package org.me.supaserver;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameSessionManager gameSessionManager;
    private final String ACCESS_KEY = "300305";

    @PostMapping("/start")
    public String startSession(@RequestHeader("X-Server-Access") String key) {
        if (!key.equals(ACCESS_KEY))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return gameSessionManager.startSession();
    }

    @PostMapping("/{gameId}/join")
    public String joinSession(@RequestHeader("X-Server-Access") String key, @PathVariable String gameId) {
        if (!key.equals(ACCESS_KEY))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return gameSessionManager.joinSession(gameId);
    }

    @GetMapping("/{gameId}/state")
    public GameState getGameState(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId) {
        if (!key.equals(ACCESS_KEY))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());
        return gameSessionManager.getGameState(gameId, token);
    }

    @PostMapping("/{gameId}/move")
    public void makeMove(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId, @RequestBody Move move) {
        if (!key.equals(ACCESS_KEY))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());

        gameSessionManager.makeMove(gameId, token, move);
    }

    @GetMapping("/{gameId}/me")
    public int getMyPlayer(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId) {
        if (!key.equals(ACCESS_KEY))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());
        return gameSessionManager.getPlayerNumber(gameId, token);
    }
}