package org.me.supaserver;

import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.stereotype.Service;

@Service
public record GameEngine() {
    public GameState nextState(GameState previousState, Move move, int player) {
        if (!moveIsLegal(previousState, move, player))
            return previousState;

        int[][] nextData = previousState.data();
        nextData[move.row()][move.column()] = previousState.player();

        boolean nextWinner = calculateWinner(nextData);

        boolean nextRunning = true;
        if (nextWinner)
            nextRunning = false;
        else if (previousState.turn() >= 9)
            nextRunning = false;

        int nextTurn = previousState.turn();
        if (nextRunning)
            nextTurn = (previousState.turn() + 1);

        int nextPlayer = previousState.player();
        if (nextRunning)
            nextPlayer = -nextPlayer;

        return new GameState(nextData, nextPlayer, nextTurn, nextWinner, nextRunning);
    }

    private boolean moveIsLegal(GameState gameState, Move move, int player) {
        if (gameState.player() != player)
            return false;
        if (move.row() > 2 || move.row() < 0 || move.column() > 2 || move.column() < 0)
            return false;
        if (gameState.data()[move.row()][move.column()] != 0)
            return false;
        if (!gameState.running())
            return false;

        return true;
    }

    private boolean calculateWinner(int[][] data) {
        for (int i = 0; i < 3; i++) {
            if (data[i][0] != 0 && data[i][0] == data[i][1] && data[i][1] == data[i][2])
                return true;
            if (data[0][i] != 0 && data[0][i] == data[1][i] && data[1][i] == data[2][i])
                return true;
        }

        if (data[1][1] != 0) {
            if (data[0][0] == data[1][1] && data[1][1] == data[2][2])
                return true;
            if (data[0][2] == data[1][1] && data[1][1] == data[2][0])
                return true;
        }

        return false;
    }
}
