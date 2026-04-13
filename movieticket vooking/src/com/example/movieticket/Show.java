package com.example.movieticket;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Show {
    private final String showId;
    private final Movie movie;
    private final LocalDateTime startTime;
    private final List<Seat> seats;

    public Show(String showId, Movie movie, LocalDateTime startTime, List<Seat> seats) {
        this.showId = showId;
        this.movie = movie;
        this.startTime = startTime;
        this.seats = new ArrayList<>(seats);
    }

    public String getShowId() {
        return showId;
    }

    public Movie getMovie() {
        return movie;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public List<Seat> getSeats() {
        return new ArrayList<>(seats);
    }

    public Seat findSeat(String seatId) {
        for (Seat seat : seats) {
            if (seat.getSeatId().equals(seatId)) {
                return seat;
            }
        }
        return null;
    }

    public int availableSeatCount() {
        int count = 0;
        for (Seat seat : seats) {
            if (!seat.isBooked()) {
                count++;
            }
        }
        return count;
    }
}
