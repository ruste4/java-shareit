package ru.practicum.shareit.requests.exceptions;

public class ItemRequestNotFound extends RuntimeException {
    public ItemRequestNotFound(String message) {
        super(message);
    }
}
