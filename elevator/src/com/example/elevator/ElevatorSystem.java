package com.example.elevator;

public class ElevatorSystem {
    public static void main(String[] args) {
        System.out.println("Elevator demo\n");

        ElevatorController controller = ElevatorController.getInstance(3, 10, 0);

        System.out.println("Adding requests...\n");
        
        controller.addRequest(2, 5, Direction.UP);
        controller.addRequest(0, 8, Direction.UP);
        controller.addRequest(7, 2, Direction.DOWN);
        controller.addRequest(4, 9, Direction.UP);
        controller.addRequest(9, 1, Direction.DOWN);

        System.out.println("Running simulation...\n");
        for (int step = 0; step < 30; step++) {
            controller.moveElevators();
            
            if (step % 5 == 0) {
                System.out.println("Step " + step);
                controller.displayStatus();
            }
            
        }

        System.out.println("Final status:");
        controller.displayStatus();

        System.out.println("Simulation complete");
    }
}
