package ru.practicum.shareit.item.dto;

import lombok.ToString;

@ToString
public class ItemDto {
    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;
}
