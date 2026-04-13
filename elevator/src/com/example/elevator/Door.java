package com.example.elevator;

public class Door {
    private boolean isOpen;
    private static final int DOOR_OPEN_TIME_MS = 1000;
    
    public Door() {
        this.isOpen = false;
    }
    
    public void open() {
        isOpen = true;
        System.out.println("Door opened");
    }
    
    public void close() {
        isOpen = false;
        System.out.println("Door closed");
    }
    
    public boolean isOpen() {
        return isOpen;
    }
    
    public void simulateDoorOperation() {
        try {
            open();
            Thread.sleep(DOOR_OPEN_TIME_MS);
            close();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
