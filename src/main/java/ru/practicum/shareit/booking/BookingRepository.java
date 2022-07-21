package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdAndStatusOrderByIdDesc(long bookerId, BookingStatus status);

    List<Booking> findAllByBookerIdOrderByIdDesc(long bookerId);

    @Query("SELECT b FROM Booking b INNER JOIN b.item Item WHERE Item.owner=:owner ORDER BY b.id DESC")
    List<Booking> findAllBookingsByItemOwner(@Param("owner") User owner);

    @Query("SELECT b FROM Booking b INNER JOIN b.item Item WHERE Item.owner=:owner AND b.status=:status " +
            "ORDER BY b.id DESC")
    List<Booking> findAllBookingsByItemOwnerWithStatus(
            @Param("owner") User owner,
            @Param("status") BookingStatus status
    );

    List<Booking> findByBookerItem(Item item);
}
