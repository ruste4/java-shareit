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

    private boolean available;

    private User owner;

    private ItemRequest request;

    @Data
    @Builder
    public static class User {

        private Long id;

        private String name;

    }

    @Data
    @Builder
    public static class ItemRequest {

        private Long id;

        private User requester;

    }

}
