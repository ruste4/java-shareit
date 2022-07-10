package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    Item addItem(long ownerId, Item item);

    Item updateItem(long ownerId, ItemDto itemDto);

    Item getItemById(long id);

    List<Item> getAllByOwnerId(long ownerId);

    List<Item> searchByNameAndDescription(String txt);
}
