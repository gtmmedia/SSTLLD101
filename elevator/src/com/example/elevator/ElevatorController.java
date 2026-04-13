package com.example.elevator;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class ElevatorController {
    private static ElevatorController instance;
    private final List<Elevator> elevators;
    private final PriorityQueue<Request> upRequests;
    private final PriorityQueue<Request> downRequests;
    private final int maxFloor;
    private final int minFloor;
    private int totalRequests;
    
    private ElevatorController(int numElevators, int maxFloor, int minFloor) {
        this.elevators = new ArrayList<>();
        this.upRequests = new PriorityQueue<>((a, b) -> Integer.compare(a.getSourceFloor(), b.getSourceFloor()));
        this.downRequests = new PriorityQueue<>((a, b) -> Integer.compare(b.getSourceFloor(), a.getSourceFloor()));
        this.maxFloor = maxFloor;
        this.minFloor = minFloor;
        this.totalRequests = 0;
        
        for (int i = 0; i < numElevators; i++) {
            elevators.add(new Elevator(i, maxFloor, minFloor));
        }
        
        System.out.println("Initialized with " + numElevators + " elevators (" + 
                          minFloor + " to " + maxFloor + " floors)\n");
    }
    
    public static ElevatorController getInstance(int numElevators, int maxFloor, int minFloor) {
        if (instance == null) {
            instance = new ElevatorController(numElevators, maxFloor, minFloor);
        }
        return instance;
    }
    
    public static ElevatorController getInstance() {
        if (instance == null) {
            throw new IllegalStateException("ElevatorController not initialized.");
        }
        return instance;
    }
    
    public void addRequest(int sourceFloor, int destinationFloor, Direction direction) {
        if (sourceFloor < minFloor || sourceFloor > maxFloor || 
            destinationFloor < minFloor || destinationFloor > maxFloor ||
            sourceFloor == destinationFloor) {
            System.out.println("Invalid request: floor out of range or same floor");
            return;
        }
        
        Request request = new Request(sourceFloor, destinationFloor, direction);
        System.out.println("Request added: " + request);
        
        if (direction == Direction.UP) {
            upRequests.offer(request);
        } else {
            downRequests.offer(request);
        }
        
        totalRequests++;
        assignElevator(request);
    }
    
    private void assignElevator(Request request) {
        Elevator bestElevator = findBestElevator(request);
        if (bestElevator != null) {
            bestElevator.addDestination(request.getSourceFloor());
            bestElevator.addDestination(request.getDestinationFloor());
            System.out.println("Assigned to elevator " + bestElevator.getId() + "\n");
        }
    }
    
    private Elevator findBestElevator(Request request) {
        Elevator bestElevator = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (Elevator elevator : elevators) {
            if (elevator.isIdle()) {
                int distance = elevator.getDistance(request.getSourceFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }
        
        if (bestElevator == null) {
            minDistance = Integer.MAX_VALUE;
            for (Elevator elevator : elevators) {
                if (elevator.getDirection() == request.getDirection()) {
                    int distance = elevator.getDistance(request.getSourceFloor());
                    if (distance < minDistance) {
                        minDistance = distance;
                        bestElevator = elevator;
                    }
                }
            }
        }
        
        if (bestElevator == null) {
            minDistance = Integer.MAX_VALUE;
            for (Elevator elevator : elevators) {
                int distance = elevator.getDistance(request.getSourceFloor());
                if (distance < minDistance) {
                    minDistance = distance;
                    bestElevator = elevator;
                }
            }
        }
        
        return bestElevator;
    }
    
    public void moveElevators() {
        for (Elevator elevator : elevators) {
            elevator.move();
        }
    }
    
    public void displayStatus() {
        System.out.println("\nElevator status:");
        for (Elevator elevator : elevators) {
            System.out.println("Elevator " + elevator.getId() + 
                    " | floor=" + elevator.getCurrentFloor() +
                    " | status=" + elevator.getStatus() +
                    " | direction=" + elevator.getDirection() +
                    " | distance=" + elevator.getTotalDistance());
        }
        System.out.println("Total Requests: " + totalRequests);
        System.out.println();
    }
    
    public List<Elevator> getElevators() {
        return elevators;
    }
    
    public int getElevatorCount() {
        return elevators.size();
    }
    
    public static void reset() {
        instance = null;
    }
}
