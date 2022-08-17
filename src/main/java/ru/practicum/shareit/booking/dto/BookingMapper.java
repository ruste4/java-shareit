package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

public class BookingMapper {
    public static BookingDto toBookingDto(Booking booking) {

        BookingDto.User itemOwner = BookingDto.User.builder()
                .id(booking.getItem().getOwner().getId())
                .name(booking.getItem().getOwner().getName())
                .build();

        BookingDto.Item item = BookingDto.Item.builder()
                .id(booking.getItem().getId())
                .name(booking.getItem().getName())
                .description(booking.getItem().getDescription())
                .owner(itemOwner)
                .build();

        BookingDto.User booker = BookingDto.User.builder()
                .id(booking.getBooker().getId())
                .name(booking.getBooker().getName())
                .build();

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(item)
                .booker(booker)
                .status(booking.getStatus().toString())
                .build();
    }

    public static Booking toBooking(BookingCreateDto bookingCreateDto) {
        return new Booking(
                bookingCreateDto.getStart(), bookingCreateDto.getEnd(), BookingStatus.WAITING
        );
    }
}
