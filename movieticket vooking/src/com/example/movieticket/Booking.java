package com.example.movieticket;

import java.util.ArrayList;
import java.util.List;

public class Booking {
    private final String bookingId;
    private final User user;
    private final Show show;
    private final List<Seat> bookedSeats;
    private BookingStatus status;

    public Booking(String bookingId, User user, Show show, List<Seat> bookedSeats) {
        this.bookingId = bookingId;
        this.user = user;
        this.show = show;
        this.bookedSeats = new ArrayList<>(bookedSeats);
        this.status = BookingStatus.CREATED;
    }

    public String getBookingId() {
        return bookingId;
    }

    public User getUser() {
        return user;
    }

    public Show getShow() {
        return show;
    }

    public List<Seat> getBookedSeats() {
        return new ArrayList<>(bookedSeats);
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        for (Seat seat : bookedSeats) {
            seat.markAvailable();
        }
        this.status = BookingStatus.CANCELLED;
    }

    public double totalAmount() {
        double total = 0.0;
        for (Seat seat : bookedSeats) {
            total += seat.getPrice();
        }
        return total;
    }
}
