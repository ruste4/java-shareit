package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Component
public class ItemStorageImpl implements ItemStorage {

    private final Map<Long, Item> storage = new HashMap<>();
    private final AtomicLong itemIdHolder = new AtomicLong();

    @Override
    public Item add(Item item) {
        item.setId(itemIdHolder.incrementAndGet());
        storage.put(item.getId(), item);

        return item;
    }

    @Override
    public Item update(Item item) {
        storage.put(item.getId(), item);

        return item;
    }

    @Override
    public Item findById(long id) {
        return storage.get(id);
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<Item> getAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public List<Item> getAllByUserId(long id) {
        return storage.values().stream()
                .filter((item) -> item.getOwner().getId() == id)
                .collect(Collectors.toList());
    }

}
