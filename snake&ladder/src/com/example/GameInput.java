package com.example;

import java.util.Scanner;

public class GameInput {
    private final Scanner scanner;

    public GameInput() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Get board size from user
     */
    public int getBoardSize() {
        System.out.print("Enter board size (n for nxn board): ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid integer: ");
            scanner.nextLine();
        }
        int size = scanner.nextInt();
        while (size <= 0) {
            System.out.print("Board size must be positive. Please enter again: ");
            size = scanner.nextInt();
        }
        // Note: Based on requirements, we assume n*n = 100, so n = 10
        if (size != 10) {
            System.out.println("Note: Adjusting to 10x10 board (100 cells) based on rules");
            size = 10;
        }
        return size;
    }

    /**
     * Get number of players from user
     */
    public int getNumberOfPlayers() {
        System.out.print("Enter number of players (minimum 2): ");
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter a valid integer: ");
            scanner.nextLine();
        }
        int numPlayers = scanner.nextInt();
        while (numPlayers < 2) {
            System.out.print("Number of players must be at least 2. Please enter again: ");
            numPlayers = scanner.nextInt();
        }
        return numPlayers;
    }

    /**
     * Get difficulty level from user
     */
    public DifficultyLevel getDifficultyLevel() {
        System.out.println("Select difficulty level:");
        System.out.println("1. EASY (continue turn on consecutive 6)");
        System.out.println("2. HARD (turn ends after three consecutive 6s)");
        System.out.print("Enter choice (1 or 2): ");

        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter 1 or 2: ");
            scanner.nextLine();
        }
        int choice = scanner.nextInt();

        while (choice != 1 && choice != 2) {
            System.out.print("Invalid choice. Please enter 1 or 2: ");
            choice = scanner.nextInt();
        }

        return choice == 1 ? DifficultyLevel.EASY : DifficultyLevel.HARD;
    }

    /**
     * Get player name
     */
    public String getPlayerName(int playerNumber) {
        System.out.print("Enter name for Player " + playerNumber + ": ");
        scanner.nextLine();  // Clear the newline
        String name = scanner.nextLine();
        while (name.trim().isEmpty()) {
            System.out.print("Name cannot be empty. Please enter again: ");
            name = scanner.nextLine();
        }
        return name.trim();
    }

    /**
     * Wait for user to press Enter (for turn-based gameplay)
     */
    public void waitForInput(String message) {
        System.out.println(message);
        scanner.nextLine();
    }

    public void close() {
        scanner.close();
    }
}
