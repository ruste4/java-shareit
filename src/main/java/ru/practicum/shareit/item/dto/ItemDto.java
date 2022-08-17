package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@EqualsAndHashCode
public class ItemDto {

    private Long id;

    private String name;

    private String description;

    private Boolean available;

    private Long requestId;

}
