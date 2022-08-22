package ru.practicum.shareit.requests.dto;

import ru.practicum.shareit.requests.ItemRequest;

public class ItemRequestMapper {
    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        ItemRequestDto.User requester = ItemRequestDto.User.builder()
                .id(itemRequest.getRequester().getId())
                .name(itemRequest.getRequester().getName())
                .build();

        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .requester(requester)
                .created(itemRequest.getCreated())
                .build();
    }

    public static ItemRequestWithResponsesDto toItemRequestWithResponsesDto(ItemRequest itemRequest) {
        return ItemRequestWithResponsesDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(itemRequest.getCreated())
                .build();
    }
}
