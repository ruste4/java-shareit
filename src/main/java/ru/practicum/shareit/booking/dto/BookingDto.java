package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode
public class BookingDto {

    private Long id;

    private LocalDate start;

    private LocalDate end;

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
