import org.me.supaclient.GameHttpClient;
import org.me.supacommonapi.GameState;
import org.me.supacommonapi.Move;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.Scanner;

private final GameHttpClient gameHttpClient = new GameHttpClient();
private final Scanner scanner = new Scanner(System.in);

void main() throws InterruptedException {
    while (true) {
        System.out.print("Enter mode (start/join): ");
        String mode = scanner.next();

        if (mode.equals("start")) {
            String gameId;
            while (true) {
                try {
                    gameId = gameHttpClient.startSession();
                    break;
                } catch (HttpStatusCodeException e) {
                    Thread.sleep(500);
                }
            }
            System.out.println("Session started with gameId: " + gameId);

            String jwt;
            while (true) {
                try {
                    jwt = gameHttpClient.joinSession(gameId);
                    break;
                } catch (HttpStatusCodeException e) {
                    Thread.sleep(500);
                }
            }

            runGame(jwt, gameId);
        } else if (mode.equals("join")) {
            System.out.print("Enter gameId: ");
            String gameId = scanner.next();

            String jwt;
            while (true) {
                try {
                    jwt = gameHttpClient.joinSession(gameId);
                    break;
                } catch (HttpStatusCodeException e) {
                    Thread.sleep(500);
                }
            }

            System.out.println("Session joined");
            runGame(jwt, gameId);
        } else {
            System.out.println("Invalid mode");
        }
    }
}

private void runGame(String jwt, String gameId) throws InterruptedException {
    boolean finalGameStateWinner;
    int finalGameStatePlayer;

    int playerNumber;
    while (true) {
        try {
            playerNumber = gameHttpClient.getMyPlayer(jwt, gameId);
            break;
        } catch (HttpStatusCodeException e) {
            Thread.sleep(500);
        }
    }
    System.out.println("You are player: " + playerToSymbol(playerNumber));

    while (true) {
        GameState gameState;
        while (true) {
            try {
                gameState = gameHttpClient.getGameState(jwt, gameId);
                break;
            } catch (HttpStatusCodeException e) {
                Thread.sleep(500);
            }
        }

        printGame(gameState);

        if (!gameState.running()) {
            finalGameStateWinner = gameState.winner();
            finalGameStatePlayer = gameState.player();
            break;
        }

        if (gameState.player() == playerNumber) {
            int row = readCoordinate("Select row: ");
            int column = readCoordinate("Select column ");

            while (true) {
                try {
                    gameHttpClient.makeMove(jwt, gameId, new Move(row - 1, column - 1));
                    break;
                } catch (HttpStatusCodeException e) {
                    Thread.sleep(500);
                }
            }

            Thread.sleep(500);
        } else {
            System.out.println("Waiting for opponent...");
            while (gameState.running() && gameState.player() != playerNumber) {
                try {
                    gameState = gameHttpClient.getGameState(jwt, gameId);
                } catch (HttpStatusCodeException e) {
                    Thread.sleep(500);
                    continue;
                }
                Thread.sleep(500);
            }
        }
    }

    if (!finalGameStateWinner)
        System.out.println("Draw");
    else if (finalGameStatePlayer == playerNumber)
        System.out.println("You win");
    else
        System.out.println("You Lose");

    System.out.println();
}

int readCoordinate(String prompt) {
    while (true) {
        System.out.print(prompt);

        String input = scanner.next();
        if (input.length() == 1) {
            char c = input.charAt(0);
            if (c == '1' || c == '2' || c == '3')
                return c - '0';
        }

        System.out.println("Invalid");
    }
}

private char playerToSymbol(int playerNumber) {
    return switch (playerNumber) {
        case 1 -> 'X';
        case -1 -> 'O';
        case 0 -> ' ';
        default -> throw new RuntimeException();
    };
}

public void printGame(GameState gameState) {
    int[][] data = gameState.data();
    System.out.println("Turn " + gameState.turn() + " | Player: " + playerToSymbol(gameState.player()));

    System.out.printf("%c | %c | %c\n", playerToSymbol(data[0][0]), playerToSymbol(data[0][1]), playerToSymbol(data[0][2]));
    System.out.println("---------");
    System.out.printf("%c | %c | %c\n", playerToSymbol(data[1][0]), playerToSymbol(data[1][1]), playerToSymbol(data[1][2]));
    System.out.println("---------");
    System.out.printf("%c | %c | %c\n\n", playerToSymbol(data[2][0]), playerToSymbol(data[2][1]), playerToSymbol(data[2][2]));

    System.out.println();
}