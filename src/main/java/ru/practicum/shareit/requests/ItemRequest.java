package ru.practicum.shareit.requests;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

@Data
@Builder
@EqualsAndHashCode
public class ItemRequest {

    private Long id;

    private String description;

    private User requester;

    private LocalDateTime created;

}
