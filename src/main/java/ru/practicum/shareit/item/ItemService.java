package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.UserIsNotBookedItemException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.requests.ItemRequest;
import ru.practicum.shareit.requests.RequestService;
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

    private final RequestService requestService;

    private final CommentRepository commentRepository;

    private final BookingRepository bookingRepository;

    /**
     * Добавить предмет
     *
     * @param ownerId id владельца предмета
     * @return предмет с сгенерированным для него номером id. Генерация id происходит в хранилище предметов
     * @throws UserNotFoundException если пользователь из поля owner не найден в системе
     */
    public Item addItem(long ownerId, ItemCreateDto itemCreateDto) {
        Item item = ItemMapper.toItem(itemCreateDto);
        User owner = checkAndGetItemOwner(ownerId, item.getName());
        item.setOwner(owner);

        if (itemCreateDto.getRequestId() != null) {
            ItemRequest itemRequest = requestService.getItemRequestById(itemCreateDto.getRequestId());
            item.setRequest(itemRequest);
        }

        log.info("Add {}", item);

        return itemRepository.save(item);
    }

    /**
     * Обновить предмет
     *
     * @param ownerId id владельца
     * @param itemDto экземпляр класса ItemDto, который содержит в себе информацию для обновления
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

    /**
     * Получить предмет
     *
     * @param itemId идентификатор предмета
     * @param userId идентификатор пользователя, который хочет получить информацию
     * @return экземпляр класса ItemWithBookingDatesDto. Экземпляр в себе содержил помимо информации о предмете еще и
     * информацию о предыдущем и следующем бронировани, если пользователь является владельцем вещи. Так же экземпляр
     * содержит в себе комментарии арендаторов
     * @throws ItemNotFoundException если предмет не найден
     */
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
     * @param itemId идентификатор предмета
     * @return найденный предмет
     * @throws ItemNotFoundException если предмет не найден
     */
    public Item getItemById(long itemId) {
        return itemRepository.findById(itemId).orElseThrow(
                () -> new ItemNotFoundException(String.format("Item with id:%s not found.", itemId))
        );
    }

    /**
     * Добавить предыдущий и следующий ближайшие брони.
     *
     * @param item предмет к которому нужно добавить информацию о бронировании
     * @return экземпляр класса ItemWithBookingDatesDto. Содержит в себе информацию из предмета, переданного в параметре
     * и дополнительно информацию о предыдущем и следующем бронях предмета
     */
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
     * @param from    - индекс первого элемента (для пагинации)
     * @param size    - количество элементов отображения
     * @return список предметов, выбранных по id владельца, с информацией и ближайших по времени бронях
     */
    public List<ItemWithBookingDatesDto> getAllByOwnerId(long ownerId, int from, int size) {
        log.info("Get all items by owner id:{}", ownerId);

        PageRequest pageRequest = PageRequest.of(from, size);

        return itemRepository.findByOwnerId(ownerId, pageRequest).stream()
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
    public List<Item> searchByNameOrDescription(String txt, int from, int size) {
        log.info("Search items by name or description with text \"{}\"", txt);

        if (txt.isBlank()) {
            return List.of();
        }

        PageRequest pageRequest = PageRequest.of(from, size);

        return itemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(txt, txt, pageRequest)
                .stream()
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
        Item item = getItemById(itemId);

        return item.getOwner().equals(owner);
    }

    /**
     * Добавить комментарии
     *
     * @param commentCreateDto экземпляр класса CommentCreateDto с текстом комментария
     * @param itemId           идентификатор предмета, к которому добавляют комментарий
     * @param userId           идентификатор пользователя, который хочет добавить комментарий
     * @return сохраненный комментарий с генерированным id и полем created
     * @throws UserNotFoundException        если пользователь по параметру userId не найден
     * @throws ItemNotFoundException        если предмет по параметру itemId не найден
     * @throws UserIsNotBookedItemException если пользователь не арендовал ранее предмет
     */
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

    /**
     * Получить все комментарии по предмету
     *
     * @param item предмет по которому происходит поиск комментария
     * @return список комментариев
     */
    public List<Comment> getAllCommentsByItem(Item item) {
        return commentRepository.findAllCommentByItem(item);
    }

    public List<Item> getAllItemByRequestId(long requestId) {
        return itemRepository.findAllByRequestId(requestId);
    }

}
