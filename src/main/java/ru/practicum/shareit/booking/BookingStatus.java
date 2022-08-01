package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.exceptions.BookingStatusException;

import java.util.List;

public enum BookingStatus {
    CURRENT("CURRENT"),
    COMPLETED("COMPLETED"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELED("CANCELED"),
    ALL("ALL");

    private final String val;

    BookingStatus(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public static BookingStatus findByName(String nameString) {
        for (BookingStatus status : values()) {
            if (status.name().equalsIgnoreCase(nameString)) {
                return status;
            }
        }

        throw new BookingStatusException(String.format("Unknown state: %s", nameString));
    }
}
