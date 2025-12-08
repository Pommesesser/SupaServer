package org.me.supacommonapi;

public record GameState(int[][] data, int player, int turn, boolean winner, boolean running) {
    public static GameState newGame() {
        return new GameState(new int[3][3], 1, 1, false, true);
    }

    public GameState(int[][] data, int player, int turn, boolean winner, boolean running) {
        this.data = deepCopy(data);
        this.player = player;
        this.turn = turn;
        this.winner = winner;
        this.running = running;
    }

    public int[][] data() {
        return deepCopy(data);
    }

    private int[][] deepCopy(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int i = 0; i < copy.length; i++)
            copy[i] = source[i].clone();

        return copy;
    }
}
