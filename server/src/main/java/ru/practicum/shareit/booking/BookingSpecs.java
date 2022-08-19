package ru.practicum.shareit.booking;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.Item_;
import ru.practicum.shareit.user.User;

import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.time.LocalDateTime;


public class BookingSpecs {
    public static Specification<Booking> hasBookingStatus(BookingStatus status) {
        return (root, query, builder) -> builder.equal(root.get(Booking_.status), status);
    }

    public static Specification<Booking> notHasBookingStatus(BookingStatus status) {
        return (root, query, builder) -> builder.notEqual(root.get(Booking_.status), status);
    }

    public static Specification<Booking> hasOwnerBookedItem(User itemOwner) {
        return (root, query, builder) -> {
            Join<Booking, Item> items = root.join(Booking_.item, JoinType.LEFT);
            return builder.equal(items.get(Item_.owner), itemOwner);
        };
    }

    public static Specification<Booking> hasBooker(User booker) {
        return (root, query, builder) -> builder.equal(root.get(Booking_.booker), booker);
    }

    public static Specification<Booking> isBookingEndGreaterThan(LocalDateTime date) {
        return (root, query, builder) -> builder.greaterThan(root.get(Booking_.end), date);
    }

    public static Specification<Booking> isBookingStartGreaterThan(LocalDateTime date) {
        return (root, query, builder) -> builder.greaterThan(root.get(Booking_.start), date);
    }

    public static Specification<Booking> isBookingEndLessThan(LocalDateTime date) {
        return (root, query, builder) -> builder.lessThan(root.get(Booking_.end), date);
    }

    public static Specification<Booking> isBookingStartLessThan(LocalDateTime date) {
        return (root, query, builder) -> builder.lessThan(root.get(Booking_.start), date);
    }
}
