package ru.practicum.shareit.booking.exceptions;

public class BookingNotFound extends RuntimeException {
    public BookingNotFound(String message) {
        super(message);
    }
}
