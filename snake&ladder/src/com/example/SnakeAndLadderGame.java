package com.example;

import java.util.*;

public class SnakeAndLadderGame {
    private Board board;
    private List<Player> players;
    private Dice dice;
    private int currentPlayerIndex;
    private DifficultyLevel difficultyLevel;
    private GameState gameState;
    private int consecutiveSixCount;
    private List<Player> winners;

    public SnakeAndLadderGame(int boardSize, List<String> playerNames, DifficultyLevel difficultyLevel) {
        this.board = new Board(boardSize);
        this.players = new ArrayList<>();
        this.dice = new Dice();
        this.currentPlayerIndex = 0;
        this.difficultyLevel = difficultyLevel;
        this.gameState = GameState.NOT_STARTED;
        this.consecutiveSixCount = 0;
        this.winners = new ArrayList<>();

        // Initialize players
        for (String name : playerNames) {
            players.add(new Player(name));
        }
    }

    /**
     * Initialize the board with random snakes and ladders
     */
    public void initializeBoard() {
        int n = board.getBoardSize();
        int maxPosition = board.getTotalCells();

        // Generate random snakes (n snakes)
        Set<Integer> usedPositions = new HashSet<>();
        Random random = new Random();

        for (int i = 0; i < n; i++) {
            int head, tail;
            do {
                head = random.nextInt(maxPosition - 1) + 2;  // 2 to maxPosition-1
                tail = random.nextInt(head - 1) + 1;         // 1 to head-1
            } while (usedPositions.contains(head) || usedPositions.contains(tail));

            board.addSnake(head, tail);
            usedPositions.add(head);
            usedPositions.add(tail);
        }

        // Generate random ladders (n ladders)
        for (int i = 0; i < n; i++) {
            int start, end;
            do {
                start = random.nextInt(maxPosition - 1) + 1;  // 1 to maxPosition-1
                end = random.nextInt(maxPosition - start) + start + 1;  // start+1 to maxPosition
            } while (usedPositions.contains(start) || usedPositions.contains(end));

            board.addLadder(start, end);
            usedPositions.add(start);
            usedPositions.add(end);
        }
    }

    /**
     * Start the game
     */
    public void startGame() {
        gameState = GameState.IN_PROGRESS;
        board.displayBoard();
        System.out.println("\n========== GAME START ==========");
        System.out.println("Difficulty Level: " + difficultyLevel);
        System.out.println("Number of Players: " + players.size());
        for (Player player : players) {
            System.out.println("  - " + player.getName());
        }
        System.out.println("================================\n");
    }

    /**
     * Play a single turn for the current player
     */
    public void playTurn() {
        if (gameState != GameState.IN_PROGRESS) {
            return;
        }

        Player currentPlayer = players.get(currentPlayerIndex);

        if (!currentPlayer.isActive()) {
            moveToNextPlayer();
            return;
        }

        System.out.println("\n--- " + currentPlayer.getName() + "'s Turn ---");
        System.out.println("Current Position: " + currentPlayer.getPosition());

        int diceValue = dice.roll();
        System.out.println("Dice Roll: " + diceValue);

        // Track consecutive 6s
        if (diceValue == 6) {
            consecutiveSixCount++;
            System.out.println("Rolled a 6! Consecutive 6s: " + consecutiveSixCount);
        } else {
            consecutiveSixCount = 0;
        }

        // Check hard difficulty condition before moving
        if (difficultyLevel == DifficultyLevel.HARD && consecutiveSixCount >= 3) {
            System.out.println("Three consecutive 6s rolled! Turn ends.");
            consecutiveSixCount = 0;
            moveToNextPlayer();
            return;
        }

        // Move the player
        int newPosition = currentPlayer.move(diceValue, board.getBoardSize());

        if (newPosition == currentPlayer.getPosition() - diceValue) {
            System.out.println("Cannot move outside the board. Position remains: " + currentPlayer.getPosition());
        } else {
            System.out.println("Moved to position: " + newPosition);

            // Check for snake or ladder
            int finalPosition = board.getFinalPosition(newPosition);
            if (finalPosition != newPosition) {
                if (board.getCell(newPosition).hasSnake()) {
                    System.out.println("Oh no! Hit a snake! Sliding down from " + newPosition + " to " + finalPosition);
                } else {
                    System.out.println("Great! Found a ladder! Climbing up from " + newPosition + " to " + finalPosition);
                }
                currentPlayer.setPosition(finalPosition);
            }

            // Check for winner
            if (currentPlayer.getPosition() == board.getTotalCells()) {
                System.out.println(currentPlayer.getName() + " wins!");
                winners.add(currentPlayer);
                currentPlayer.setActive(false);

                // Check if we should end the game (at least 2 players still playing)
                int activePlayers = countActivePlayers();
                if (activePlayers < 2) {
                    gameState = GameState.FINISHED;
                    endGame();
                    return;
                }
            }
        }

        // Decide if player continues or turn passes
        boolean continuesTurn = false;
        if (diceValue == 6) {
            if (difficultyLevel == DifficultyLevel.EASY) {
                continuesTurn = true;
                System.out.println("Rolled a 6! Continue turn.");
            } else if (consecutiveSixCount < 3) {
                continuesTurn = true;
                System.out.println("Rolled a 6! Continue turn. (Consecutive: " + consecutiveSixCount + "/3)");
            }
        }

        if (!continuesTurn) {
            consecutiveSixCount = 0;
            moveToNextPlayer();
        }
    }

    /**
     * Move to the next active player
     */
    private void moveToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (!players.get(currentPlayerIndex).isActive() && countActivePlayers() > 0);
    }

    /**
     * Count the number of active players
     */
    private int countActivePlayers() {
        return (int) players.stream().filter(Player::isActive).count();
    }

    /**
     * End the game
     */
    private void endGame() {
        System.out.println("\n========== GAME OVER ==========");
        System.out.println("Winners (in order):");
        for (int i = 0; i < winners.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + winners.get(i).getName());
        }
        System.out.println("==============================\n");
    }

    /**
     * Check if the game is finished
     */
    public boolean isGameFinished() {
        return gameState == GameState.FINISHED;
    }

    /**
     * Get the current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Display current game status
     */
    public void displayStatus() {
        System.out.println("\n========== GAME STATUS ==========");
        for (Player player : players) {
            String status = player.isActive() ? "PLAYING" : "FINISHED";
            System.out.println(player.getName() + " - Position: " + player.getPosition() + " - Status: " + status);
        }
        System.out.println("================================\n");
    }
}
