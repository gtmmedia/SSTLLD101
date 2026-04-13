package com.example;

public class Player {
    private final String name;
    private int position;           // Current position on the board (0 = outside board)
    private boolean isActive;       // Whether player is still in the game

    public Player(String name) {
        this.name = name;
        this.position = 0;           // Start outside the board
        this.isActive = true;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    /**
     * Move the player by the given number of cells
     * Returns the new position after processing snakes/ladders
     */
    public int move(int diceValue, int boardSize) {
        int newPosition = this.position + diceValue;
        
        // Check if the move is outside the board
        int maxPosition = boardSize * boardSize;
        if (newPosition > maxPosition) {
            // Piece does not move if it goes outside
            return this.position;
        }
        
        this.position = newPosition;
        return this.position;
    }

    @Override
    public String toString() {
        return "Player{" +
                "name='" + name + '\'' +
                ", position=" + position +
                ", isActive=" + isActive +
                '}';
    }
}
