package com.example;

public class Snake {
    private int head;      // Where the snake's head is (larger number)
    private int tail;      // Where the snake's tail is (smaller number)

    public Snake(int head, int tail) {
        if (head <= tail) {
            throw new IllegalArgumentException("Snake head must be greater than tail");
        }
        this.head = head;
        this.tail = tail;
    }

    public int getHead() {
        return head;
    }

    public int getTail() {
        return tail;
    }

    @Override
    public String toString() {
        return "Snake{" +
                "head=" + head +
                ", tail=" + tail +
                '}';
    }
}
