package com.example.parkinglot;

public class ParkingSpot {
    private final String spotId;
    private final VehicleType allowedType;
    private Vehicle parkedVehicle;

    public ParkingSpot(String spotId, VehicleType allowedType) {
        this.spotId = spotId;
        this.allowedType = allowedType;
    }

    public String getSpotId() {
        return spotId;
    }

    public boolean isFree() {
        return parkedVehicle == null;
    }

    public boolean canPark(Vehicle vehicle) {
        return isFree() && vehicle.getVehicleType() == allowedType;
    }

    public boolean park(Vehicle vehicle) {
        if (!canPark(vehicle)) {
            return false;
        }
        parkedVehicle = vehicle;
        return true;
    }

    public void removeVehicle() {
        parkedVehicle = null;
    }
}
