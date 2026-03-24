# Snake and Ladder Game - Low Level Design (LLD)

## Overview
A complete implementation of the Snake and Ladder game following SOLID principles and object-oriented design patterns.

## Requirements Implemented

### Core Features
- ✅ Configurable board size (n × n)
- ✅ Multiple players (minimum 2)
- ✅ Two difficulty levels (Easy/Hard)
- ✅ Random snake and ladder placement (n snakes and n ladders)
- ✅ Turn-based gameplay with 6-sided dice
- ✅ Snake and ladder mechanics
- ✅ Winner determination

### Game Rules Implemented
- ✅ Board positions: 1 to n²
- ✅ Players start at position 0 (outside board)
- ✅ Dice generates random value 1-6
- ✅ Forward movement by dice value
- ✅ Snake descent (head → tail, smaller number)
- ✅ Ladder ascent (start → end, larger number)
- ✅ Win condition: reach position n²
- ✅ Boundary check: piece doesn't move if it goes beyond n²
- ✅ Game continues until at least 2 players remain

### Difficulty Levels
- **EASY**: Player continues turn after rolling consecutive 6
- **HARD**: Player's turn ends after rolling three consecutive 6s

## Class Design

### 1. **DifficultyLevel.java**
- **Purpose**: Enum to represent game difficulty
- **Values**: EASY, HARD
- **Usage**: Controls turn continuation rules

### 2. **Dice.java**
- **Purpose**: Simulates a 6-sided dice
- **Methods**: 
  - `roll()`: Returns random value 1-6
- **Responsibility**: Single Responsibility - Only handles dice rolling

### 3. **Snake.java**
- **Purpose**: Represents a snake entity
- **Attributes**:
  - `head`: Position where snake starts (larger number)
  - `tail`: Position where snake ends (smaller number)
- **Validation**: Ensures head > tail

### 4. **Ladder.java**
- **Purpose**: Represents a ladder entity
- **Attributes**:
  - `start`: Position where ladder starts (smaller number)
  - `end`: Position where ladder ends (larger number)
- **Validation**: Ensures start < end

### 5. **Cell.java**
- **Purpose**: Represents a single cell on the board
- **Attributes**:
  - `position`: Cell number
  - `snake`: Optional snake entity
  - `ladder`: Optional ladder entity
- **Methods**:
  - `hasSnake()`, `hasLadder()`: Check cell contents
  - `getSnake()`, `getLadder()`: Retrieve entities

### 6. **Player.java**
- **Purpose**: Represents a game player
- **Attributes**:
  - `name`: Player identifier
  - `position`: Current board position
  - `isActive`: Game status
- **Methods**:
  - `move(diceValue, boardSize)`: Move player with boundary check
  - Turn tracking and management

### 7. **Board.java**
- **Purpose**: Manages the game board
- **Attributes**:
  - `cells[]`: Array of Cell objects
  - `snakes`: Map of position → Snake
  - `ladders`: Map of position → Ladder
- **Methods**:
  - `addSnake()`, `addLadder()`: Add game entities
  - `getFinalPosition()`: Get position after snake/ladder effect
  - `displayBoard()`: Show board configuration
- **Validation**: Prevents snake/ladder conflicts

### 8. **GameState.java**
- **Purpose**: Enum for game states
- **Values**: NOT_STARTED, IN_PROGRESS, FINISHED

### 9. **GameInput.java**
- **Purpose**: Handle user input and interaction
- **Methods**:
  - `getBoardSize()`: Get board dimensions
  - `getNumberOfPlayers()`: Get player count
  - `getDifficultyLevel()`: Select difficulty
  - `getPlayerName()`: Input player names
  - `waitForInput()`: Continue game flow
- **Responsibility**: Input validation and user interface

### 10. **SnakeAndLadderGame.java**
- **Purpose**: Main game controller and orchestrator
- **Attributes**:
  - `board`: Game board instance
  - `players`: List of players
  - `dice`: Dice instance
  - `currentPlayerIndex`: Turn tracking
  - `consecutiveSixCount`: Difficulty rule tracking
  - `winners`: List of winners in order
- **Methods**:
  - `initializeBoard()`: Random snake/ladder placement
  - `startGame()`: Initialize and display game
  - `playTurn()`: Execute single player turn
  - `isGameFinished()`: Check game state
  - `displayStatus()`: Show player positions
- **Responsibility**: Game flow orchestration

### 11. **Main.java**
- **Purpose**: Entry point for the application
- **Flow**:
  1. Get game parameters from user
  2. Create game instance
  3. Initialize board with random snakes/ladders
  4. Execute main game loop until finished

## Design Patterns Used

### 1. **Single Responsibility Principle (SRP)**
- Each class has one clear responsibility
- Dice only rolls
- Player only manages position
- Board only manages entities

### 2. **Separation of Concerns**
- `GameInput`: UI/Input handling
- `Board`: Data management
- `SnakeAndLadderGame`: Game logic
- `Player`: Entity state

### 3. **Encapsulation**
- Private attributes with public getters
- Validation in setters
- Protected game state

### 4. **Object-Oriented Design**
- Snake and Ladder as distinct classes
- Player abstraction
- Cell composition pattern

## Game Flow

```
1. User inputs board size, player count, difficulty
2. Game creates board and random snakes/ladders
3. Main loop:
   - Current player rolls dice
   - Player moves forward
   - Check for snake/ladder
   - Check for winner
   - Check consecutive 6s (difficulty rules)
   - Pass turn or continue
   - Repeat until game finished
4. Display winners
```

## Difficulty Rule Implementation

### EASY Mode
```
Roll 6 → Continue turn
Other → Pass to next player
```

### HARD Mode
```
1st consecutive 6 → Continue turn
2nd consecutive 6 → Continue turn
3rd consecutive 6 → Turn ends
Non-6 → Reset counter
```

## Compilation and Execution

```bash
# Compile
javac -d . src/com/example/*.java

# Run
java com.example.Main
```

## Sample Input/Output

```
========== SNAKE AND LADDER GAME ==========

Enter board size (n for nxn board): 10
Enter number of players (minimum 2): 2
Select difficulty level:
1. EASY (continue turn on consecutive 6)
2. HARD (turn ends after three consecutive 6s)
Enter choice (1 or 2): 1
Enter name for Player 1: Alice
Enter name for Player 2: Bob

========== BOARD CONFIG ==========
Board Size: 10x10 (Total Cells: 100)

Snakes:
  Snake{head=78, tail=45}
  ...

Ladders:
  Ladder{start=15, end=45}
  ...
==================================

========== GAME START ==========
Difficulty Level: EASY
Number of Players: 2
  - Alice
  - Bob
================================

--- Alice's Turn ---
Current Position: 0
Dice Roll: 4
Moved to position: 4
Press Enter to continue...
```

## Key Features

1. **Random Board Generation**: Snakes and ladders placed randomly each game
2. **Validation**: Input validation and error handling
3. **Turn Management**: Proper turn rotation with difficulty rules
4. **Winner Tracking**: Winners recorded in order
5. **Game Status**: Real-time display of player positions

## Edge Cases Handled

- Boundary overflow: Piece doesn't move if it exceeds board size
- Snake/Ladder conflict: No two entities at same position
- Invalid positions: Validation for head/tail and start/end
- Minimum players: At least 2 players required
- Game end condition: Game ends when only 1 active player remains
