package ru.practicum.shareit.item;

import java.util.List;

public interface ItemStorage {
    Item add(Item item);

    Item update(Item item);

    Item findById(long id);

    void deleteById(long id);

    List<Item> getAll();
    List<Item> getAllByUserId(long id);
}
