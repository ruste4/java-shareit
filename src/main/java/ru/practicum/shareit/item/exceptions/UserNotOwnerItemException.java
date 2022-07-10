package ru.practicum.shareit.item.exceptions;

public class UserNotOwnerItemException extends RuntimeException {
    public UserNotOwnerItemException(String message) {
        super(message);
    }
}
