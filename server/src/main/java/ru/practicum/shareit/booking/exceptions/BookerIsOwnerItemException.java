package ru.practicum.shareit.booking.exceptions;

public class BookerIsOwnerItemException extends RuntimeException {
    public BookerIsOwnerItemException(String message) {
        super(message);
    }
}
