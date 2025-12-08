package org.me.supaserver;

import org.me.supacommonapi.GameState;

public record GameSession(int players, GameState gameState, long creationTime) {}