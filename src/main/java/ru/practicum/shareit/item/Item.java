package ru.practicum.shareit.item;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.user.User;

@Data
@Builder
@EqualsAndHashCode
public class Item {

    private Long id;

    private String name;

    private String description;

    private boolean available;

    private User owner;

    private ItemRequest request;

}
