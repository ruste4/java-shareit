package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<Comment> comments;

    @Data
    @Builder
    public static class Comment {

        private Long id;

        private String text;

        private String authorName;

        private LocalDateTime created;

    }

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
