package com.example.elevator;

import java.util.PriorityQueue;

public class Elevator {
    private final int id;
    private int currentFloor;
    private ElevatorStatus status;
    private Direction direction;
    private final Door door;
    private final PriorityQueue<Integer> upQueue;
    private final PriorityQueue<Integer> downQueue;
    private int totalDistance;
    
    public Elevator(int id, int maxFloor, int minFloor) {
        this.id = id;
        this.currentFloor = minFloor;
        this.status = ElevatorStatus.IDLE;
        this.direction = Direction.IDLE;
        this.door = new Door();
        this.totalDistance = 0;
        this.upQueue = new PriorityQueue<>();
        this.downQueue = new PriorityQueue<>((a, b) -> Integer.compare(b, a));
    }
    
    public void addDestination(int floor) {
        if (floor > currentFloor) {
            upQueue.offer(floor);
        } else if (floor < currentFloor) {
            downQueue.offer(floor);
        }
    }
    
    public void moveUp() {
        if (!upQueue.isEmpty()) {
            int nextFloor = upQueue.peek();
            if (currentFloor < nextFloor) {
                status = ElevatorStatus.MOVING;
                direction = Direction.UP;
                currentFloor++;
                totalDistance++;
                System.out.println("Elevator " + id + " -> floor " + currentFloor);
            } else if (currentFloor == nextFloor) {
                upQueue.poll();
                stop();
            }
        } else if (!downQueue.isEmpty()) {
            direction = Direction.DOWN;
            moveDown();
        } else {
            setIdle();
        }
    }
    
    public void moveDown() {
        if (!downQueue.isEmpty()) {
            int nextFloor = downQueue.peek();
            if (currentFloor > nextFloor) {
                status = ElevatorStatus.MOVING;
                direction = Direction.DOWN;
                currentFloor--;
                totalDistance++;
                System.out.println("Elevator " + id + " -> floor " + currentFloor);
            } else if (currentFloor == nextFloor) {
                downQueue.poll();
                stop();
            }
        } else if (!upQueue.isEmpty()) {
            direction = Direction.UP;
            moveUp();
        } else {
            setIdle();
        }
    }
    
    public void stop() {
        status = ElevatorStatus.STOPPED;
        System.out.println("Elevator " + id + " stopped at floor " + currentFloor);
        door.simulateDoorOperation();

        if (!upQueue.isEmpty()) {
            direction = Direction.UP;
            status = ElevatorStatus.IDLE;
        } else if (!downQueue.isEmpty()) {
            direction = Direction.DOWN;
            status = ElevatorStatus.IDLE;
        } else {
            setIdle();
        }
    }
    
    private void setIdle() {
        status = ElevatorStatus.IDLE;
        direction = Direction.IDLE;
        System.out.println("Elevator " + id + " idle at floor " + currentFloor);
    }
    
    public void move() {
        if (direction == Direction.UP) {
            moveUp();
        } else if (direction == Direction.DOWN) {
            moveDown();
        }
    }
    
    public int getDistance(int floor) {
        return Math.abs(currentFloor - floor);
    }
    
    public boolean isIdle() {
        return status == ElevatorStatus.IDLE && upQueue.isEmpty() && downQueue.isEmpty();
    }
    
    public int getId() {
        return id;
    }
    
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    public ElevatorStatus getStatus() {
        return status;
    }
    
    public Direction getDirection() {
        return direction;
    }
    
    public int getTotalDistance() {
        return totalDistance;
    }
    
    @Override
    public String toString() {
        return "Elevator{" +
                "id=" + id +
                ", floor=" + currentFloor +
                ", status=" + status +
                ", direction=" + direction +
                '}';
    }
}
