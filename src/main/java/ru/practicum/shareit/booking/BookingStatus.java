package ru.practicum.shareit.booking;

public enum BookingStatus {
    CURRENT("CURRENT"),
    COMPLETED("COMPLETED"),
    FUTURE("FUTURE"),
    WAITING("WAITING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED"),
    CANCELED("CANCELED");

    private final String val;

    BookingStatus(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
