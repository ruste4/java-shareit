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

    private List<Item> items;

    @Data
    public static class Item {
        private Long id;

        private String name;

        private Long ownerId;

        private String description;

        private Boolean available;

        private Long requestId;

        public Item(ru.practicum.shareit.item.Item item) {
            this.id = item.getId();
            this.name = item.getName();
            this.ownerId = item.getOwner().getId();
            this.description = item.getDescription();
            this.available = item.isAvailable();
            this.requestId = item.getRequest().getId();
        }
    }
}
