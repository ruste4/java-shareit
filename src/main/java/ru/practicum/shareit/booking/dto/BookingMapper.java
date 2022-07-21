package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemService itemService;
    private final UserService userService;
    public BookingDto toBookingDto(Booking booking) {

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

    public Booking toBooking(BookingCreateDto bookingCreateDto, long ownerId) {
        Item item = itemService.getItemById(bookingCreateDto.getItemId());
        User booker = userService.getUserById(ownerId);

        return new Booking(
                item, bookingCreateDto.getStart(), bookingCreateDto.getEnd(), booker, BookingStatus.WAITING
        );
    }
}
