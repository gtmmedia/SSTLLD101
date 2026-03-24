package com.example;

import java.util.Random;

public class Dice {
    private static final int DICE_SIDES = 6;
    private Random random;

    public Dice() {
        this.random = new Random();
    }

    /**
     * Roll the dice and return a value between 1 and 6
     */
    public int roll() {
        return random.nextInt(DICE_SIDES) + 1;
    }
}
