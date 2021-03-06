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

    /**
     * Добавить предмет
     * @param ownerId id владельца предмета
     * @param item предмет
     * @return предмет с сгенерированным для него номером id. Генерация id происходит в хранилище предметов
     * @throws UserNotFoundException если пользователь из поля owner не найден в системе
     */
    @Override
    public Item addItem(long ownerId, Item item) {
        User owner = checkAndGetItemOwner(ownerId, item.getName());
        item.setOwner(owner);
        log.info("Add {}", item);

        return itemStorage.add(item);
    }

    /**
     * Обновить предмет
     * @param ownerId id владельца
     * @param itemDto
     * @return обновленный предмет из хранилища
     * @throws UserNotFoundException если владелец не найден в системе
     * @throws UserNotOwnerItemException если пользователь не является хозяином предмета
     */
    @Override
    public Item updateItem(long ownerId, ItemDto itemDto) {
        checkAndGetItemOwner(ownerId, itemDto.getName());

        if (!isOwnerOfItem(ownerId, itemDto.getId())) {
            throw new UserNotOwnerItemException(
                    String.format("The user with id:%s is not the owner of the %s", ownerId, itemDto)
            );
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

    /**
     * Получить предмет по id
     * @param id
     * @return найденный предмет
     */
    @Override
    public Item getItemById(long id) {
        log.info("Get item by id:{}", id);
        return itemStorage.findById(id);
    }

    /**
     * Получить все предметы определенного владельца
     * @param ownerId id владельца
     * @return список предметов, выбранных по id владельца
     */
    @Override
    public List<Item> getAllByOwnerId(long ownerId) {
        log.info("Get all items by owner id:{}", ownerId);
        return itemStorage.getAllByUserId(ownerId);
    }

    /**
     * Найти предметы по имени и описанию
     * @param txt текст по наличию котого будет вестись поиск
     * @return список предметов, в которых нашлось совпадение по переданному тексту. Если ничего не нашлось, вернет
     * пустой список
     */
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
                    String.format("Owner with id:%d not found for item '%s'", ownerId, itemName)
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
