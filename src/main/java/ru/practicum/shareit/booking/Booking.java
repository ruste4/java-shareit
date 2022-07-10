package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDate;

@Data
@Builder
@EqualsAndHashCode
public class Booking {

    private Long id;

    private LocalDate start;

    private LocalDate end;

    private Item item;

    private User booker;

    private BookingStatus status;

}
