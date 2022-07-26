package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.exceptions.BookingNotFound;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.UserIsNotBookedItemException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserService userService;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    /**
     * Добавить предмет
     *
     * @param ownerId id владельца предмета
     * @param item    предмет
     * @return предмет с сгенерированным для него номером id. Генерация id происходит в хранилище предметов
     * @throws UserNotFoundException если пользователь из поля owner не найден в системе
     */
    public Item addItem(long ownerId, Item item) {
        User owner = checkAndGetItemOwner(ownerId, item.getName());
        item.setOwner(owner);
        log.info("Add {}", item);

        return itemRepository.save(item);
    }

    /**
     * Обновить предмет
     *
     * @param ownerId id владельца
     * @param itemDto
     * @return обновленный предмет из хранилища
     * @throws UserNotFoundException     если владелец не найден в системе
     * @throws UserNotOwnerItemException если пользователь не является хозяином предмета
     */
    public Item updateItem(long ownerId, ItemDto itemDto) {
        checkAndGetItemOwner(ownerId, itemDto.getName());

        if (!isOwnerOfItem(ownerId, itemDto.getId())) {
            throw new UserNotOwnerItemException(
                    String.format("The user with id:%s is not the owner of the %s", ownerId, itemDto)
            );
        }

        Item itemInStorage = itemRepository.findById(itemDto.getId()).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with id:%s not found.", itemDto.getId()))
        );

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

        return itemInStorage;
    }


    public ItemWithBookingDatesDto getItemById(long itemId, long userId) {

        log.info("Get item by id:{}", itemId);

        Item item = getItemById(itemId);
        ItemWithBookingDatesDto itemDto = ItemMapper.toItemWithBookingDatesDto(item);

        if (isOwnerOfItem(userId, itemId)) {
            itemDto = addToItemLastAndNextBooking(item);
        }

        List<Comment> comments = getAllCommentsByItem(item);
        itemDto.setComments(
                comments.stream()
                        .map(c -> ItemWithBookingDatesDto.Comment.builder()
                                .id(c.getId())
                                .text(c.getText())
                                .authorName(c.getAuthor().getName())
                                .created(c.getCreated())
                                .build())
                        .collect(Collectors.toList())
        );

        return itemDto;

    }


    /**
     * Получить предмет по id
     *
     * @return найденный предмет
     */
    public Item getItemById(long itemId) {
        log.info("Get item by id:{}", itemId);

        return itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with id:%s not found.", itemId))
        );
    }

    private ItemWithBookingDatesDto addToItemLastAndNextBooking(Item item) {

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findAllBookingByItemId(item.getId());

        Optional<Booking> lastBooking = bookings.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd));

        Optional<Booking> nextBooking = bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart));

        ItemWithBookingDatesDto itemWithBookingDatesDto = ItemMapper.toItemWithBookingDatesDto(item);

        lastBooking.ifPresent(itemWithBookingDatesDto::setLastBooking);
        nextBooking.ifPresent(itemWithBookingDatesDto::setNextBooking);

        return itemWithBookingDatesDto;
    }

    /**
     * Получить все предметы определенного владельца
     *
     * @param ownerId id владельца
     * @return список предметов, выбранных по id владельца
     */
    public List<ItemWithBookingDatesDto> getAllByOwnerId(long ownerId) {
        log.info("Get all items by owner id:{}", ownerId);

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(this::addToItemLastAndNextBooking)
                .collect(Collectors.toList());
    }

    /**
     * Найти предметы по имени и описанию
     *
     * @param txt текст по наличию котого будет вестись поиск
     * @return список предметов, в которых нашлось совпадение по переданному тексту. Если ничего не нашлось, вернет
     * пустой список
     */
    public Set<Item> searchByNameAndDescription(String txt) {
        log.info("Search items by name and description with text \"{}\"", txt);

        if (txt.isBlank()) {
            return Set.of();
        }

        return itemRepository.findByNameContainingIgnoreCase(txt)
                .and(itemRepository.findByDescriptionContainingIgnoreCase(txt))
                .filter(Item::isAvailable)
                .toSet();
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
        Item item = getItemById(itemId);

        return item.getOwner().equals(owner);
    }

    public Comment addComment(CommentCreateDto commentCreateDto, long itemId, long userId) {
        User author = userService.getUserById(userId);
        Item item = itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with id:%s not found.", itemId))
        );
        boolean authorIsBookedItem = bookingRepository.findAllBookingByItem(item).stream()
                .anyMatch(
                        b -> b.getBooker().getId().equals(userId)
                                && !b.getStatus().equals(BookingStatus.REJECTED)
                                && item.isAvailable()
                                && b.getEnd().isBefore(LocalDateTime.now())
                );

        if (!authorIsBookedItem) {
            throw new UserIsNotBookedItemException(
                    String.format("User with id:%s did not book the item with id:%s", userId, itemId)
            );
        }

        Comment comment = Comment.builder()
                .text(commentCreateDto.getText())
                .author(author)
                .item(item)
                .created(LocalDateTime.now())
                .build();

        return commentRepository.save(comment);
    }

    public List<Comment> getAllCommentsByItem(Item item) {
        return commentRepository.findAllCommentByItem(item);
    }


}
