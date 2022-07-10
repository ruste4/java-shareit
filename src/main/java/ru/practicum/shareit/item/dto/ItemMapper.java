package ru.practicum.shareit.item.dto;

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
        Item.ItemBuilder itemBuilder = Item.builder();

        if (itemDto.getId() != null) {
            itemBuilder.id(itemDto.getId());
        }

        if (itemDto.getName() != null) {
            itemBuilder.name(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            itemBuilder.description(itemDto.getDescription());
        }

        if (itemDto.getAvailable() == null) {
            itemBuilder.available(false);
        } else {
            itemBuilder.available(itemDto.getAvailable());
        }

        return itemBuilder.build();
    }

    public static Item toItem(ItemCreateDto itemCreateDto) {
        return Item.builder()
                .name(itemCreateDto.getName())
                .description(itemCreateDto.getDescription())
                .available(itemCreateDto.getAvailable())
                .build();
    }
}
