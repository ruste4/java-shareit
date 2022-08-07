package ru.practicum.shareit.requests.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class ItemRequestWithResponsesDto {
    private Long id;

    private String description;

    private LocalDateTime created;

    private List<Item> responses;

    @Data
    public static class Item {
        private Long itemId;

        private String name;

        private Long ownerId;

        public Item(ru.practicum.shareit.item.Item item) {
            this.itemId = item.getId();
            this.name = item.getName();
            this.ownerId = item.getOwner().getId();
        }
    }
}
