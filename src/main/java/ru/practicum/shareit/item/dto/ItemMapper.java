package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Item;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto.User owner = ItemDto.User.builder()
                .id(item.getOwner().getId())
                .name(item.getOwner().getName())
                .build();

        ItemDto.User requester = ItemDto.User.builder()
                .id(item.getRequest().getRequester().getId())
                .name(item.getRequest().getRequester().getName())
                .build();

        ItemDto.ItemRequest request = ItemDto.ItemRequest.builder()
                .id(item.getRequest().getId())
                .requester(requester)
                .build();

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.isAvailable())
                .owner(owner)
                .request(request)
                .build();
    }
}
