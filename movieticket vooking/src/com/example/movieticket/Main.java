package com.example.movieticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        BookingService bookingService = new BookingService();

        Movie movie = new Movie("M1", "Inception", 148);

        List<Seat> seats = new ArrayList<>();
        seats.add(new Seat("A1", SeatType.REGULAR, 150));
        seats.add(new Seat("A2", SeatType.REGULAR, 150));
        seats.add(new Seat("A3", SeatType.PREMIUM, 250));
        seats.add(new Seat("A4", SeatType.PREMIUM, 250));

        Show show = bookingService.createShow("S1", movie, LocalDateTime.now().plusHours(2), seats);

        User user = new User("U1", "Alex");
        Booking booking = bookingService.createBooking(user, show.getShowId(), Arrays.asList("A1", "A3"));

        System.out.println("Booking successful");
        System.out.println("Booking ID: " + booking.getBookingId());
        System.out.println("User: " + booking.getUser().getName());
        System.out.println("Movie: " + booking.getShow().getMovie().getTitle());
        System.out.println("Amount: " + booking.totalAmount());
        System.out.println("Available seats left: " + show.availableSeatCount());
    }
}
