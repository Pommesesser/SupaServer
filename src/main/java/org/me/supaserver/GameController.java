package org.me.supaserver;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/game")
public class GameController {
    @Autowired
    private GameSessionManager gameSessionManager;
    @Value("${server.access-key}")
    private String accessKey;

    @PostMapping("/start")
    public String startSession(@RequestHeader("X-Server-Access") String key) {
        if (!key.equals(accessKey))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (gameSessionManager.serverIsFull())
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Server full");

        return gameSessionManager.startSession();
    }

    @PostMapping("/{gameId}/join")
    public String joinSession(@RequestHeader("X-Server-Access") String key, @PathVariable String gameId) {
        if (!key.equals(accessKey))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        return gameSessionManager.joinSession(gameId);
    }

    @GetMapping("/{gameId}/state")
    public GameState getGameState(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId) {
        if (!key.equals(accessKey))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());
        return gameSessionManager.getGameState(gameId, token);
    }

    @PostMapping("/{gameId}/move")
    public void makeMove(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId, @RequestBody Move move) {
        if (!key.equals(accessKey))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());

        gameSessionManager.makeMove(gameId, token, move);
    }

    @GetMapping("/{gameId}/me")
    public int getMyPlayer(@RequestHeader("X-Server-Access") String key, @RequestHeader("Authorization") String authorization, @PathVariable String gameId) {
        if (!key.equals(accessKey))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        if (authorization == null || !authorization.startsWith("Bearer "))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        String token = authorization.substring("Bearer ".length());
        return gameSessionManager.getPlayerNumber(gameId, token);
    }
}