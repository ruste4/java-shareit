package ru.practicum.shareit.booking.exceptions;

public class BookingAlreadyApprovedException extends RuntimeException {
    public BookingAlreadyApprovedException(String message) {
        super(message);
    }
}
