package com.example;

public class Ladder {
    private int start;     // Where the ladder starts (smaller number)
    private int end;       // Where the ladder ends (larger number)

    public Ladder(int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("Ladder start must be less than end");
        }
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "Ladder{" +
                "start=" + start +
                ", end=" + end +
                '}';
    }
}
