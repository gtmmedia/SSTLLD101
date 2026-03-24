package com.example;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        GameInput input = new GameInput();

        try {
            // Get game parameters from user
            System.out.println("========== SNAKE AND LADDER GAME ==========\n");

            int boardSize = input.getBoardSize();
            int numPlayers = input.getNumberOfPlayers();
            DifficultyLevel difficulty = input.getDifficultyLevel();

            // Get player names
            List<String> playerNames = new ArrayList<>();
            for (int i = 1; i <= numPlayers; i++) {
                playerNames.add(input.getPlayerName(i));
            }

            // Create and initialize game
            SnakeAndLadderGame game = new SnakeAndLadderGame(boardSize, playerNames, difficulty);
            game.initializeBoard();
            game.startGame();

            // Main game loop
            while (!game.isGameFinished()) {
                game.playTurn();
                if (!game.isGameFinished()) {
                    input.waitForInput("Press Enter to continue...");
                    game.displayStatus();
                }
            }

        } finally {
            input.close();
        }
    }
}
