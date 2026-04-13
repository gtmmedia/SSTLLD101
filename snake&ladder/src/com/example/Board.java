package com.example;

import java.util.*;

public class Board {
    private final int boardSize;
    private final Cell[] cells;
    private final Map<Integer, Snake> snakes;
    private final Map<Integer, Ladder> ladders;

    public Board(int boardSize) {
        this.boardSize = boardSize;
        int totalCells = boardSize * boardSize;
        this.cells = new Cell[totalCells + 1];  // 1-indexed
        this.snakes = new HashMap<>();
        this.ladders = new HashMap<>();

        // Initialize all cells
        for (int i = 1; i <= totalCells; i++) {
            cells[i] = new Cell(i);
        }
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getTotalCells() {
        return boardSize * boardSize;
    }

    /**
     * Add a snake to the board at the specified head position
     */
    public void addSnake(int head, int tail) {
        if (head <= 0 || head > getTotalCells() || tail <= 0 || tail >= head) {
            throw new IllegalArgumentException("Invalid snake position");
        }
        if (isSnakeLadderConflict(head)) {
            throw new IllegalArgumentException("Position " + head + " already has a snake or ladder");
        }

        Snake snake = new Snake(head, tail);
        cells[head].setSnake(snake);
        snakes.put(head, snake);
    }

    /**
     * Add a ladder to the board at the specified start position
     */
    public void addLadder(int start, int end) {
        if (start <= 0 || start > getTotalCells() || end <= start || end > getTotalCells()) {
            throw new IllegalArgumentException("Invalid ladder position");
        }
        if (isSnakeLadderConflict(start)) {
            throw new IllegalArgumentException("Position " + start + " already has a snake or ladder");
        }

        Ladder ladder = new Ladder(start, end);
        cells[start].setLadder(ladder);
        ladders.put(start, ladder);
    }

    /**
     * Check if a position already has a snake or ladder
     */
    private boolean isSnakeLadderConflict(int position) {
        return cells[position].hasSnake() || cells[position].hasLadder();
    }

    /**
     * Get the cell at the specified position
     */
    public Cell getCell(int position) {
        if (position < 0 || position > getTotalCells()) {
            throw new IllegalArgumentException("Invalid position");
        }
        return cells[position];
    }

    /**
     * Get the final position after considering snakes and ladders
     */
    public int getFinalPosition(int position) {
        Cell cell = getCell(position);

        // Check for snake
        if (cell.hasSnake()) {
            return cell.getSnake().getTail();
        }

        // Check for ladder
        if (cell.hasLadder()) {
            return cell.getLadder().getEnd();
        }

        return position;
    }

    /**
     * Display the board with snakes and ladders
     */
    public void displayBoard() {
        System.out.println("\n========== BOARD CONFIG ==========");
        System.out.println("Board Size: " + boardSize + "x" + boardSize + " (Total Cells: " + getTotalCells() + ")");

        System.out.println("\nSnakes:");
        if (snakes.isEmpty()) {
            System.out.println("  None");
        } else {
            snakes.forEach((head, snake) ->
                    System.out.println("  " + snake)
            );
        }

        System.out.println("\nLadders:");
        if (ladders.isEmpty()) {
            System.out.println("  None");
        } else {
            ladders.forEach((start, ladder) ->
                    System.out.println("  " + ladder)
            );
        }
        System.out.println("==================================\n");
    }
}
