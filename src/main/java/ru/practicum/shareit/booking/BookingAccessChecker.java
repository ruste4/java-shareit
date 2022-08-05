package ru.practicum.shareit.booking;

import ru.practicum.shareit.user.User;

public interface BookingAccessChecker<T extends Booking, U extends User> {
    void check(T t, U u);
}
