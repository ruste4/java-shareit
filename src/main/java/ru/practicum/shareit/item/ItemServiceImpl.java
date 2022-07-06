package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    @Autowired
    public ItemServiceImpl(ItemStorage itemStorage, UserService userService) {
        this.itemStorage = itemStorage;
        this.userService = userService;
    }

    @Override
    public Item addItem(long ownerId, Item item) {
        User owner = checkAndGetItemOwner(ownerId, item.getName());
        item.setOwner(owner);
        log.info("Add {}", item);

        return itemStorage.add(item);
    }

    @Override
    public Item updateItem(long ownerId, ItemDto itemDto) {
        checkAndGetItemOwner(ownerId, itemDto.getName());

        if (!isOwnerOfItem(ownerId, itemDto.getId())) {
            throw new UserNotOwnerItemException("The user with id:" + ownerId + " is not the owner of the " + itemDto);
        }

        Item itemInStorage = itemStorage.findById(itemDto.getId());

        if (itemDto.getAvailable() != null) {
            itemInStorage.setAvailable(itemDto.getAvailable());
        }

        if (itemDto.getDescription() != null) {
            itemInStorage.setDescription(itemDto.getDescription());
        }

        if (itemDto.getName() != null) {
            itemInStorage.setName(itemDto.getName());
        }

        log.info("Update item with id:{} on {}", itemInStorage.getId(), itemInStorage);
        itemStorage.update(itemInStorage);

        return itemInStorage;
    }

    @Override
    public Item getItemById(long id) {
        log.info("Get item by id:{}", id);
        return itemStorage.findById(id);
    }

    @Override
    public List<Item> getAllByOwnerId(long ownerId) {
        log.info("Get all items by owner id:{}", ownerId);
        return itemStorage.getAllByUserId(ownerId);
    }

    @Override
    public List<Item> searchByNameAndDescription(String txt) {
        log.info("Search items by name and description with text \"{}\"", txt);

        if (txt.isBlank()) {
            return List.of();
        }

        return itemStorage.getAll().stream()
                .filter((item) -> item.getName().toLowerCase().contains(txt.toLowerCase())
                        || item.getDescription().toLowerCase().contains(txt.toLowerCase()))
                .filter(Item::isAvailable)
                .collect(Collectors.toList());
    }

    /**
     * Проверить и вернуть владельца вещи
     *
     * @param ownerId id владельца
     * @return владельца вещи
     * @throws UserNotFoundException если пользователь с переданным id не зарегестрирован в системе
     */
    private User checkAndGetItemOwner(long ownerId, String itemName) {
        User owner = userService.getUserById(ownerId);

        if (owner == null) {
            throw new UserNotFoundException(
                    "Owner with id:" + ownerId + " not found for item \"" + itemName + "\""
            );
        }

        return owner;
    }

    /**
     * Хозяин ли вещи?
     *
     * @param userId id пользователя
     * @param itemId id вещи
     * @return true - если пользователь является хозяеном вещи
     */
    private boolean isOwnerOfItem(long userId, long itemId) {
        User owner = userService.getUserById(userId);
        Item item = itemStorage.findById(itemId);

        return item.getOwner().equals(owner);
    }
}
