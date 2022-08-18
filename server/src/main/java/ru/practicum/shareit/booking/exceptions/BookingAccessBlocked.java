package ru.practicum.shareit.booking.exceptions;

public class BookingAccessBlocked extends RuntimeException {
    public BookingAccessBlocked(String message) {
        super(message);
    }
}
