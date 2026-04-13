package com.example.movieticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BookingService {
    private final Map<String, Show> shows = new HashMap<>();
    private final Map<String, Booking> bookings = new HashMap<>();

    public Show createShow(String showId, Movie movie, LocalDateTime startTime, List<Seat> seats) {
        Show show = new Show(showId, movie, startTime, seats);
        shows.put(showId, show);
        return show;
    }

    public Show getShow(String showId) {
        return shows.get(showId);
    }

    public Booking createBooking(User user, String showId, List<String> seatIds) {
        Show show = shows.get(showId);
        if (show == null) {
            throw new IllegalArgumentException("Show not found: " + showId);
        }

        List<Seat> selectedSeats = new ArrayList<>();
        for (String seatId : seatIds) {
            Seat seat = show.findSeat(seatId);
            if (seat == null) {
                throw new IllegalArgumentException("Seat not found: " + seatId);
            }
            if (seat.isBooked()) {
                throw new IllegalStateException("Seat already booked: " + seatId);
            }
            selectedSeats.add(seat);
        }

        for (Seat seat : selectedSeats) {
            seat.markBooked();
        }

        String bookingId = UUID.randomUUID().toString();
        Booking booking = new Booking(bookingId, user, show, selectedSeats);
        booking.confirm();
        bookings.put(bookingId, booking);
        return booking;
    }

    public void cancelBooking(String bookingId) {
        Booking booking = bookings.get(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found: " + bookingId);
        }
        booking.cancel();
    }

    public List<Show> listShows() {
        return new ArrayList<>(shows.values());
    }
}
