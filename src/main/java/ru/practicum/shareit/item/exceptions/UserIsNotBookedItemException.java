package ru.practicum.shareit.item.exceptions;

public class UserIsNotBookedItemException extends RuntimeException {
    public UserIsNotBookedItemException(String message) {
        super(message);
    }
}
