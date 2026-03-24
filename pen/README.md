# Pen Design System - LLD Assignment

## Overview
This is a comprehensive Low-Level Design (LLD) for a **Pen System** that models different types of pens with realistic behavior and components.

## Design Patterns Used

### 1. **Strategy Pattern**
- Different pen types (Ballpoint, Gel, Fountain) implement different writing behaviors
- Each pen subclass extends the abstract `Pen` class and provides its own `write()` implementation

### 2. **Factory Pattern**
- `PenFactory` class creates pens without exposing internal details
- Supports both standard pen creation and custom pen creation with specific parameters

### 3. **Composition**
- Pens are composed of `Refill` objects
- `Refill` contains `Ink` and `Nib`
- `Ink` contains `Color` information

## Class Structure

### Core Components

#### 1. **Color** (`Color.java`)
- Represents the color of ink
- Properties:
  - `name`: Color name (e.g., "Black", "Red")
  - `hexCode`: Hex representation (e.g., "#000000")

#### 2. **Nib** (`Nib.java`)
- Represents the writing tip of a pen
- Properties:
  - `sizeInMm`: Size of the nib (e.g., 1.0 mm)
  - `material`: Material (e.g., "Steel", "Ceramic", "Gold")
  - `nibType`: Enum (BALLPOINT, GEL, FOUNTAIN)
- Methods:
  - `write()`: Demonstrates writing action

#### 3. **Ink** (`Ink.java`)
- Manages ink properties and consumption
- Properties:
  - `color`: Color object
  - `quantityInMl`: Amount of ink available
  - `type`: Ink type (e.g., "Oil-based", "Water-based gel")
  - `viscosity`: Ink viscosity (0-1 scale)
- Methods:
  - `depleteInk()`: Reduces ink quantity (simulates writing)
  - `refill()`: Adds more ink
  - `isEmpty()`: Checks if pen has ink
  - `getters/setters`

#### 4. **Refill** (`Refill.java`)
- Represents the replaceable part of a pen
- Contains both `Ink` and `Nib`
- Methods:
  - `replaceInk()`: Replace ink cartridge
  - `replaceNib()`: Replace nib
  - `getters`

#### 5. **Pen (Abstract)** (`Pen.java`)
- Base class for all pen types
- Properties:
  - `brand`: Pen brand name
  - `model`: Model name
  - `refill`: Refill object
  - `isOpen`: State of the pen (open/closed)
- Abstract Methods:
  - `write()`: Each pen type writes differently
  - `getType()`: Returns pen type string
- Concrete Methods:
  - `open()`: Opens the pen
  - `close()`: Closes the pen
  - `replaceRefill()`: Replaces the refill
  - `hasInk()`: Checks ink availability

### Pen Types (Concrete Classes)

#### 1. **BallpointPen** (`BallpointPen.java`)
- Represents a ballpoint pen
- Additional Properties:
  - `ballDiameter`: Size of the ball
  - `ballMaterial`: Material of the ball (e.g., "Tungsten Carbide")
- Writing Behavior:
  - Consumes 0.1 ml ink per write
  - Most durable, writes on most surfaces

#### 2. **GelPen** (`GelPen.java`)
- Represents a gel pen
- Additional Properties:
  - `gelDensity`: Density of gel
  - `isPressurized`: Whether it uses pressurized flow
- Writing Behavior:
  - Consumes 0.15 ml ink per write
  - Smooth writing experience
  - Pressure-based ink flow

#### 3. **FountainPen** (`FountainPen.java`)
- Represents a fountain pen
- Additional Properties:
  - `tipWidth`: Width of the nib/tip
  - `tipTaper`: Taper style (e.g., "Tapered")
  - `isInked`: Whether pen is inked
- Writing Behavior:
  - Consumes 0.05 ml ink per write (most efficient)
  - Elegant writing
  - Requires dipping/refilling in ink

### Factory

#### **PenFactory** (`PenFactory.java`)
- Static factory methods for creating pens
- Methods:
  - `createBallpointPen()`: Creates a standard ballpoint pen
  - `createGelPen()`: Creates a standard gel pen
  - `createFountainPen()`: Creates a standard fountain pen
  - `createCustomPen()`: Creates custom pens with specific parameters

## Key Features

### 1. **Pen States**
- Pens can be opened and closed
- Cannot write when closed

### 2. **Ink Management**
- Tracks ink quantity in ml
- Ink depletes with writing
- Different pen types consume ink at different rates:
  - Ballpoint: 0.1 ml/write
  - Gel: 0.15 ml/write
  - Fountain: 0.05 ml/write

### 3. **Refill System**
- Replaceable refills with ink and nib
- Can replace refill completely
- Can change ink color

### 4. **Type Safety**
- Nib types defined as enum
- Different nib materials for different pen types

## Usage Example

```java
// Create a color
Color black = new Color("Black", "#000000");

// Create a ballpoint pen using factory
Pen pen = PenFactory.createBallpointPen("Parker", "Jotter", black);

// Open and write
pen.open();
pen.write("Hello, World!");
pen.close();

// Check ink status
System.out.println("Ink: " + pen.getRefill().getInk().getQuantityInMl() + " ml");

// Replace refill
Color blue = new Color("Blue", "#0000FF");
Ink newInk = new Ink(blue, 10.0, "Oil-based", 0.8);
Nib newNib = new Nib(1.0, "Steel", Nib.NibType.BALLPOINT);
Refill newRefill = new Refill(newInk, newNib);
pen.replaceRefill(newRefill);
```

## Files Structure

```
pen/
├── src/
│   └── com/
│       └── example/
│           ├── Color.java          // Color representation
│           ├── Nib.java            // Nib/tip component
│           ├── Ink.java            // Ink management
│           ├── Refill.java         // Replaceable refill
│           ├── Pen.java            // Abstract base class
│           ├── BallpointPen.java   // Concrete implementation
│           ├── GelPen.java         // Concrete implementation
│           ├── FountainPen.java    // Concrete implementation
│           ├── PenFactory.java     // Factory class
│           └── Main.java           // Demo/Test file
└── README.md                        // This file
```

## Design Principles Applied

### 1. **Single Responsibility Principle (SRP)**
- Each class has a single, well-defined responsibility
- `Color` handles color, `Ink` handles ink, etc.

### 2. **Open/Closed Principle (OCP)**
- Open for extension (new pen types can be added)
- Closed for modification (existing code doesn't change)

### 3. **Liskov Substitution Principle (LSP)**
- All pen subclasses can be used interchangeably
- Each correctly implements the `Pen` contract

### 4. **Dependency Inversion Principle (DIP)**
- Depends on abstractions (`Pen` abstract class)
- Not on concrete implementations

## Testing/Demo

Run the `Main` class to see demonstrations of:
- Creating different pen types
- Writing with pens
- Ink depletion
- Refill replacement
- Custom pen creation

## Possible Extensions

1. **WritingStyle**: Add handwriting style quality ratings
2. **Durability**: Track and rate pen durability
3. **PriceCalculator**: Calculate pen pricing
4. **PenCollection**: Manage a collection of pens
5. **ExpenseTracker**: Track how much ink is spent
6. **PenMaintenance**: Track maintenance schedules
7. **WritePatterns**: Different writing patterns for different use cases
