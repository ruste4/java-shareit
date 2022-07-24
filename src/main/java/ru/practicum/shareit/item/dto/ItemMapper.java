package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.dto.ItemWithBookingDatesDto;
import ru.practicum.shareit.item.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }

    public static Item toItem(ItemDto itemDto) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable());
    }

    public static Item toItem(ItemCreateDto itemCreateDto) {
        return new Item(itemCreateDto.getName(),
                itemCreateDto.getDescription(),
                itemCreateDto.getAvailable());
    }

    public static ItemWithBookingDatesDto toItemWithBookingDatesDto(Item item) {

        return ItemWithBookingDatesDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .build();
    }
}
