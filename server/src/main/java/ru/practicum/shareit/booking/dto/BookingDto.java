package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode
public class BookingDto {

    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Item item;

    private User booker;

    private String status;

    @Data
    @Builder
    public static class Item {

        Long id;

        String name;

        String description;

        User owner;
    }

    @Data
    @Builder
    public static class User {

        private Long id;

        private String name;

    }
}
