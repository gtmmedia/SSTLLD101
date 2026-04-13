package com.example.elevator;

public class Request {
    private final int sourceFloor;
    private final int destinationFloor;
    private final Direction direction;
    private final long timestamp;
    
    public Request(int sourceFloor, int destinationFloor, Direction direction) {
        this.sourceFloor = sourceFloor;
        this.destinationFloor = destinationFloor;
        this.direction = direction;
        this.timestamp = System.currentTimeMillis();
    }
    
    public int getSourceFloor() {
        return sourceFloor;
    }
    
    public int getDestinationFloor() {
        return destinationFloor;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "Request{" +
                "from=" + sourceFloor +
                ", to=" + destinationFloor +
                ", direction=" + direction +
                ", time=" + timestamp +
                '}';
    }
}
