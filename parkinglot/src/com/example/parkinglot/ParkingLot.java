package com.example.parkinglot;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ParkingLot {
    private final String name;
    private final List<ParkingFloor> floors;
    private final Map<String, Ticket> activeTickets = new HashMap<>();
    private final Map<VehicleType, Integer> hourlyRates = new HashMap<>();

    public ParkingLot(String name, List<ParkingFloor> floors) {
        this.name = name;
        this.floors = floors;
        hourlyRates.put(VehicleType.BIKE, 20);
        hourlyRates.put(VehicleType.CAR, 40);
    }

    public Ticket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAndPark(vehicle);
            if (spot != null) {
                String ticketId = UUID.randomUUID().toString();
                Ticket ticket = new Ticket(ticketId, vehicle, spot.getSpotId(), LocalDateTime.now());
                activeTickets.put(ticketId, ticket);
                System.out.println("Vehicle parked in spot " + spot.getSpotId() + " on floor " + floor.getFloorId());
                return ticket;
            }
        }
        throw new IllegalStateException("No spot available for " + vehicle.getVehicleType());
    }

    public double unparkVehicle(String ticketId) {
        Ticket ticket = activeTickets.get(ticketId);
        if (ticket == null) {
            throw new IllegalArgumentException("Invalid ticket: " + ticketId);
        }

        ParkingSpot spot = findSpot(ticket.getSpotId());
        if (spot == null) {
            throw new IllegalStateException("Spot not found: " + ticket.getSpotId());
        }

        LocalDateTime exitTime = LocalDateTime.now();
        long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
        long hours = Math.max(1, (minutes + 59) / 60);
        double fee = hours * hourlyRates.get(ticket.getVehicle().getVehicleType());

        spot.removeVehicle();
        ticket.closeTicket(exitTime, fee);
        activeTickets.remove(ticketId);

        System.out.println("Vehicle unparked from spot " + ticket.getSpotId());
        System.out.println("Parking fee: " + fee);
        return fee;
    }

    private ParkingSpot findSpot(String spotId) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findSpotById(spotId);
            if (spot != null) {
                return spot;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
