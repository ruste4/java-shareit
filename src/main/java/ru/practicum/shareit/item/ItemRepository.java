package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByOwnerId(Long ownerId);

    Streamable<Item> findByNameContainingIgnoreCase(String name);

    Streamable<Item> findByDescriptionContainingIgnoreCase(String description);

    List<Item> findAllByRequestId(long requestId);
}
