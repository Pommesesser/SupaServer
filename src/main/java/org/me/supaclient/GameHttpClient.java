package org.me.supaclient;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Service
public class GameHttpClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String accessKey = "300305";
    private final String baseUrl = "http://localhost:8080/game";

    public String startSession() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Server-Access", accessKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(
                    baseUrl + "/start",
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (HttpStatusCodeException e) {
            System.out.println("Error " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString());
            throw e;
        }

        return response.getBody();
    }

    public String joinSession(String gameId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Server-Access", accessKey);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<String> response;

        try {
            response = restTemplate.exchange(baseUrl + "/" + gameId + "/join",
                    HttpMethod.POST,
                    request,
                    String.class);
        } catch (HttpStatusCodeException e) {
            System.out.println("Error " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString());
            throw e;
        }

        return response.getBody();
    }

    public GameState getGameState(String jwt, String gameId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Server-Access", accessKey);
        headers.setBearerAuth(jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<GameState> response;

        try {
            response = restTemplate.exchange(baseUrl + "/" + gameId + "/state",
                    HttpMethod.GET,
                    request,
                    GameState.class);
        } catch (HttpStatusCodeException e) {
            System.out.println("Error " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString());
            throw e;
        }

        return response.getBody();
    }

    public void makeMove(String jwt, String gameId, Move move) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Server-Access", accessKey);
        headers.setBearerAuth(jwt);
        HttpEntity<Move> request = new HttpEntity<>(move, headers);
        try {
            restTemplate.exchange(baseUrl + "/" + gameId + "/move",
                    HttpMethod.POST,
                    request,
                    Void.class);
        } catch (HttpStatusCodeException e) {
            System.out.println("Error " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString());
            throw e;
        }
    }

    public int getMyPlayer(String jwt, String gameId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Server-Access", accessKey);
        headers.setBearerAuth(jwt);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Integer> response;

        try {
            response = restTemplate.exchange(baseUrl + "/" + gameId + "/me",
                    HttpMethod.GET,
                    request,
                    Integer.class);
        } catch (HttpStatusCodeException e) {
            System.out.println("Error " + e.getStatusCode().value() + " - " + e.getResponseBodyAsString());
            throw e;
        }

        return response.getBody();
    }
}
