package ru.practicum.shareit.booking;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;
import java.time.LocalDateTime;

@StaticMetamodel(Booking.class)
public class Booking_ {
    public static volatile SingularAttribute<Booking, LocalDateTime> start;
    public static volatile SingularAttribute<Booking, LocalDateTime> end;
    public static volatile SingularAttribute<Booking, BookingStatus> status;
    public static volatile SingularAttribute<Booking, User> booker;
    public static volatile SingularAttribute<Booking, Item> item;
}
