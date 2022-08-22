package ru.practicum.shareit.item.exceptions;

public class ItemIsNotAvailableException extends RuntimeException {
    public ItemIsNotAvailableException(String message) {
        super(message);
    }
}
