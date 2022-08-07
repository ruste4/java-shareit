package ru.practicum.shareit.requests.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemRequestCreateDto {
    private Long requesterId;

    private String description;
}
