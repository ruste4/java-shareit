package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.Booking;

@Data
@Builder
@EqualsAndHashCode
public class ItemWithBookingDatesDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Booking lastBooking;

    private Booking nextBooking;

    public void setLastBooking(ru.practicum.shareit.booking.Booking lastBooking) {
        this.lastBooking = new Booking(lastBooking.getId(), lastBooking.getBooker().getId());
    }

    public void setNextBooking(ru.practicum.shareit.booking.Booking nextBooking) {
        this.nextBooking = new Booking(nextBooking.getId(), nextBooking.getBooker().getId());
    }

    @Data
    @AllArgsConstructor
    public static class Booking {

        private Long id;

        private long bookerId;

    }
}
