package com.example.parkinglot;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        ParkingFloor floor1 = new ParkingFloor("F1", Arrays.asList(
                new ParkingSpot("F1-B1", VehicleType.BIKE),
                new ParkingSpot("F1-B2", VehicleType.BIKE),
                new ParkingSpot("F1-C1", VehicleType.CAR)
        ));

        ParkingFloor floor2 = new ParkingFloor("F2", Arrays.asList(
                new ParkingSpot("F2-B1", VehicleType.BIKE),
                new ParkingSpot("F2-C1", VehicleType.CAR),
                new ParkingSpot("F2-C2", VehicleType.CAR)
        ));

        ParkingLot parkingLot = new ParkingLot("City Center Parking", Arrays.asList(floor1, floor2));

        Vehicle bike = new Vehicle("BIKE-101", VehicleType.BIKE);
        Vehicle car = new Vehicle("CAR-501", VehicleType.CAR);

        Ticket bikeTicket = parkingLot.parkVehicle(bike);
        Ticket carTicket = parkingLot.parkVehicle(car);

        parkingLot.unparkVehicle(bikeTicket.getTicketId());
        parkingLot.unparkVehicle(carTicket.getTicketId());
    }
}
