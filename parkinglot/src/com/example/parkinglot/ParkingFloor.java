package com.example.parkinglot;

import java.util.ArrayList;
import java.util.List;

public class ParkingFloor {
    private final String floorId;
    private final List<ParkingSpot> spots;

    public ParkingFloor(String floorId, List<ParkingSpot> spots) {
        this.floorId = floorId;
        this.spots = new ArrayList<>(spots);
    }

    public String getFloorId() {
        return floorId;
    }

    public ParkingSpot findAndPark(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (spot.park(vehicle)) {
                return spot;
            }
        }
        return null;
    }

    public ParkingSpot findSpotById(String spotId) {
        for (ParkingSpot spot : spots) {
            if (spot.getSpotId().equals(spotId)) {
                return spot;
            }
        }
        return null;
    }
}
