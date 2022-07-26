package ru.practicum.shareit.booking;

public interface BookingChecker<T extends Booking> {
    void check(T t);
}
