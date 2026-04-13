package com.example.movieticket;

public class Seat {
    private final String seatId;
    private final SeatType seatType;
    private final double price;
    private boolean booked;

    public Seat(String seatId, SeatType seatType, double price) {
        this.seatId = seatId;
        this.seatType = seatType;
        this.price = price;
        this.booked = false;
    }

    public String getSeatId() {
        return seatId;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public double getPrice() {
        return price;
    }

    public boolean isBooked() {
        return booked;
    }

    public void markBooked() {
        this.booked = true;
    }

    public void markAvailable() {
        this.booked = false;
    }
}
