package ru.practicum.shareit.booking.exceptions;

public class BookerIsNotOwnerItemException extends RuntimeException {
    public BookerIsNotOwnerItemException(String message) {
        super(message);
    }
}
