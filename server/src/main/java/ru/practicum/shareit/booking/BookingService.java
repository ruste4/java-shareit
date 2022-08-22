package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.exceptions.ItemIsNotAvailableException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.shareit.booking.BookingSpecs.*;
import static ru.practicum.shareit.booking.BookingStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    private final ItemService itemService;

    /**
     * Добавить бронирование
     *
     * @param userId           идентификатор пользователя, который хочеть добавить бронь
     * @param bookingCreateDto экземпляр класса BookingCreateDto котором передаются данные для регистрации брони
     * @return бронирование с генерированным id
     * @throws UserNotFoundException                  если пользователь с переданным id не зарегестрирован в системе
     * @throws BookerIsOwnerItemException             если пользователь является владельцем вещи. Исключение
     *                                                выбрасывается из лямбда-выражения BookingChecker<Booking>
     *                                                bookerIsNotOwnerItem
     * @throws ItemIsNotAvailableException            если вещь не доступна для бронирования. Исключение выбрасывается
     *                                                из лямбда-выражения BookingChecker<Booking> bookingIsAvailable
     * @throws BookingIncorrectStartEndDatesException если даты старта и конца бронирования в прошлом времени или дата
     *                                                старта после конца бронирования. Исключение выбрасывается из
     *                                                лямбда-выражения BookingChecker<Booking> bookingDatesIsCorrect
     */
    public Booking addBooking(BookingCreateDto bookingCreateDto, long userId) {
        User booker = userService.getUserById(userId);
        Item item = itemService.getItemById(bookingCreateDto.getItemId());

        Booking booking = BookingMapper.toBooking(bookingCreateDto);
        booking.setBooker(booker);
        booking.setItem(item);

        log.info("Add booking {}", booking);

        bookerIsNotOwnerItem.check(booking);
        bookingIsAvailable.check(booking);
        bookingDatesIsCorrect.check(booking);

        return bookingRepository.save(booking);
    }

    private final BookingChecker<Booking> bookerIsNotOwnerItem = (booking) -> {
        boolean bookerIsOwnerItem = booking.getBooker().equals(booking.getItem().getOwner());
        if (bookerIsOwnerItem) {
            throw new BookerIsOwnerItemException(
                    String.format(
                            "Booker with id:%s is owner item with id:%s.",
                            booking.getBooker().getId(),
                            booking.getItem().getId()
                    )
            );
        }
    };

    private final BookingChecker<Booking> bookingIsAvailable = (booking) -> {
        if (!booking.getItem().isAvailable()) {
            throw new ItemIsNotAvailableException(
                    String.format("Item with id:%s is not available.", booking.getItem().getId())
            );
        }
    };

    private final BookingChecker<Booking> bookingDatesIsCorrect = (booking) -> {
        boolean isCorrectDates = booking.getStart().isBefore(LocalDateTime.now())
                || booking.getEnd().isBefore(LocalDateTime.now())
                || booking.getStart().isAfter(booking.getEnd());
        if (isCorrectDates) {
            throw new BookingIncorrectStartEndDatesException("Start or end of booking in the past");
        }
    };

    /**
     * Получить бронирование по id
     *
     * @param bookingId id бронирования
     * @param userId    идентификатор пользователя, который хочет получить информацию о бронировании
     * @return экземпляр класса Booking
     * @throws BookingNotFound      если бронирование не найдено по переданному id
     * @throws BookingAccessBlocked если пользователь не является арендатором либо арендодателем вещи. Исключение
     *                              выбрасывается из лямбда-выражения
     *                              BookingAccessChecker<Booking, User> accessIsAllowedToBooking
     */
    public Booking getBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFound(String.format("Booking with id:%s not found", bookingId)));

        User user = userService.getUserById(userId);

        accessIsAllowedToBooking.check(booking, user);

        return booking;
    }

    private final BookingAccessChecker<Booking, User> accessIsAllowedToBooking = (booking, user) -> {
        if (!booking.getBooker().equals(user) && !booking.getItem().getOwner().equals(user)) {
            throw new BookingAccessBlocked(String.format("Booking with id:%s access blocked.", booking.getId()));
        }
    };

    /**
     * Подтверить бронирование
     *
     * @param bookingId  id бронирования
     * @param userId     id владельца
     * @param isApproved true если владелец подтвердил / false если владелец не подтвердил
     * @return бронирование с обновленным полем approved
     * @throws BookingNotFound                 если бронирование не был найден по переданному id
     * @throws UserNotFoundException           если пользователь по параметру userId не найден
     * @throws UserNotOwnerItemException       если подтвердить бронирование пытается не владелец вещи. Исключение
     *                                         выбрасывается из лямбда-выражения
     *                                         BookingAccessChecker<Booking, User> userIsOwnerBooking
     * @throws BookingAlreadyApprovedException если бронь уже подтверждена
     */
    public Booking approveBooking(long bookingId, long userId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFound(String.format("Booking with id:%s not found", bookingId)));

        User user = userService.getUserById(userId);

        userIsOwnerBooking.check(booking, user);

        if (booking.getStatus().equals(APPROVED) && isApproved) {
            throw new BookingAlreadyApprovedException(String.format("Booking with id:%s already approved.", bookingId));
        }

        if (isApproved) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(BookingStatus.REJECTED);
        }

        return booking;
    }

    private final BookingAccessChecker<Booking, User> userIsOwnerBooking = (booking, user) -> {
        User itemOwner = booking.getItem().getOwner();

        if (!itemOwner.equals(user)) {
            throw new UserNotOwnerItemException(String.format("User with id: %s is not owner of item", user.getId()));
        }
    };

    public List<Booking> getAllBookingsCurrentUser(long userId, String status, int from, int size) {
        User booker = userService.getUserById(userId);
        BookingStatus bookingStatus = BookingStatus.findByName(status);
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("id").descending());

        switch (bookingStatus) {
            case FUTURE:
                return bookingRepository.findAll(
                        BookingSpecs
                                .hasBooker(booker)
                                .and(hasBookingStatus(APPROVED).or(hasBookingStatus(WAITING)))
                                .and(BookingSpecs.isBookingStartGreaterThan(now)),
                        pageRequest).toList();

            case CURRENT:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasBooker(booker)
                                        .and(hasBookingStatus(APPROVED).or(hasBookingStatus(REJECTED)))
                                        .and(isBookingStartLessThan(now).and(isBookingEndGreaterThan(now))),
                                pageRequest)
                        .toList();
            case PAST:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasBooker(booker)
                                        .and(hasBookingStatus(APPROVED))
                                        .and(isBookingEndLessThan(now)),
                                pageRequest)
                        .toList();
            case REJECTED:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasBooker(booker)
                                        .and(hasBookingStatus(REJECTED)),
                                pageRequest)
                        .toList();
            case WAITING:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasBooker(booker)
                                        .and(hasBookingStatus(WAITING)),
                                pageRequest)
                        .toList();
        }

        return bookingRepository.findAll(pageRequest).toList();
    }

    /**
     * Получить все брони по арендатору
     *
     * @param booker арендатор
     * @return список броней выбранных по bookerId
     */
    public List<Booking> getAllByBooker(User booker) {
        return bookingRepository.findAllByBookerOrderByIdDesc(booker);
    }

    public List<Booking> getAllBookingsForItemsOwner(long itemOwnerId, String status, int from, int size) {
        User itemOwner = userService.getUserById(itemOwnerId);
        BookingStatus bookingStatus = BookingStatus.findByName(status);
        LocalDateTime now = LocalDateTime.now();
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("id").descending());

        switch (bookingStatus) {
            case FUTURE:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasOwnerBookedItem(itemOwner)
                                        .and(hasBookingStatus(APPROVED).or(hasBookingStatus(WAITING)))
                                        .and(BookingSpecs.isBookingStartGreaterThan(now)),
                                pageRequest)
                        .toList();

            case CURRENT:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasOwnerBookedItem(itemOwner)
                                        .and(hasBookingStatus(APPROVED).or(hasBookingStatus(REJECTED)))
                                        .and(isBookingStartLessThan(now).and(isBookingEndGreaterThan(now))),
                                pageRequest)
                        .toList();
            case PAST:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasOwnerBookedItem(itemOwner)
                                        .and(hasBookingStatus(APPROVED))
                                        .and(isBookingEndLessThan(now)),
                                pageRequest)
                        .toList();
            case REJECTED:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasOwnerBookedItem(itemOwner)
                                        .and(hasBookingStatus(REJECTED)),
                                pageRequest)
                        .toList();
            case WAITING:
                return bookingRepository.findAll(
                                BookingSpecs
                                        .hasOwnerBookedItem(itemOwner)
                                        .and(hasBookingStatus(WAITING)),
                                pageRequest)
                        .toList();
        }

        return bookingRepository.findAll(
                BookingSpecs.hasOwnerBookedItem(itemOwner),
                pageRequest
        ).toList();
    }

    /**
     * Получить все брони по арендодателю
     *
     * @param itemOwnerId идентификатор арендодателя
     * @return список броней, выбранных по itemOwnerId
     */
    public List<Booking> getAllByItemOwnerId(long itemOwnerId) {
        User itemOwner = userService.getUserById(itemOwnerId);

        return bookingRepository.findAllBookingsByItemOwner(itemOwner);
    }
}
