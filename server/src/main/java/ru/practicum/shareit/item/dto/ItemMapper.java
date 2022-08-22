package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto.ItemDtoBuilder result = ItemDto.builder();
        result.id(item.getId());
        result.name(item.getName());
        result.description(item.getDescription());
        result.available(item.isAvailable());

        if (item.getRequest() != null) {
            result.requestId(item.getRequest().getId());
        }

        return result.build();
    }

    public static ItemCreateDto toItemCreateDto(Item item) {
        ItemCreateDto.ItemCreateDtoBuilder result = ItemCreateDto.builder();
        result.name(item.getName());
        result.description(item.getDescription());
        result.available(item.isAvailable());

        if (item.getRequest() != null) {
            result.requestId(item.getRequest().getId());
        }

        return result.build();
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
