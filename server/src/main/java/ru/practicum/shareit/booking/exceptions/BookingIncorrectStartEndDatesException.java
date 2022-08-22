package ru.practicum.shareit.booking.exceptions;

public class BookingIncorrectStartEndDatesException extends RuntimeException {
    public BookingIncorrectStartEndDatesException(String message) {
        super(message);
    }
}
