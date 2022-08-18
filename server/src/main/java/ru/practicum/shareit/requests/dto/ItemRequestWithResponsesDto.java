package ru.practicum.shareit.requests.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ItemRequestWithResponsesDto {
    private Long id;

    private String description;

    private LocalDateTime created;

    private List<Item> responses;

    @Data
    public static class Item {
        private Long id;

        private String name;

        private Long ownerId;

        public Item(ru.practicum.shareit.item.Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.ownerId = item.getOwner().getId();
        }
    }
}
