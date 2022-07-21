package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.item.exceptions.ItemIsNotAvailableException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;

    /**
     * Добавить бронирование
     *
     * @param booking DTO объект для создания бронирования
     * @return бронирование с генерированным id
     * @throws UserNotFoundException если пользователь с переданным id не зарегестрирован в системе
     */
    public Booking addBooking(Booking booking) {
        log.info("Add booking {}", booking);

        bookerIsNotOwnerItem.check(booking);
        bookingIsAvailable.check(booking);
        bookingDatesIsCorrect.check(booking);

        return bookingRepository.save(booking);
    }

    private final BookingChecker<Booking> bookerIsNotOwnerItem = (booking) -> {
        boolean bookerIsOwnerItem = booking.getBooker().equals(booking.getItem().getOwner());
        if (bookerIsOwnerItem) {
            throw new BookerIsNotOwnerItemException(
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
     * @return бронирование
     * @throws BookingNotFound если бронирование не найдено по переданному id
     */
    public Booking getBookingById(long bookingId, long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFound(String.format("Booking with id:%s not found", bookingId)));

        User user = userService.getUserById(userId);

        accessIsAllowedToBooking.check(booking, user);

        return booking;
    }

    private final BookingAccessChecker<Booking, User> accessIsAllowedToBooking= (booking, user) -> {
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
     * @throws BookingNotFound           если бронирование не был найден по переданному id
     * @throws UserNotOwnerItemException если подтвердить бронирование пытается не владелец вещи
     */
    public Booking approveBooking(long bookingId, long userId, boolean isApproved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFound(String.format("Booking with id:%s not found", bookingId)));

        User user = userService.getUserById(userId);

        userIsOwnerBooking.check(booking, user);

        if (booking.getStatus().equals(BookingStatus.APPROVED)) {
            throw new BookingAlreadyApprovedException(String.format("Booking with id:%s already approved.", bookingId));
        }

        if (isApproved) {
            booking.setStatus(BookingStatus.APPROVED);
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

    public List<Booking> getAllByBookerWithStatus(long bookerId, String status) {
        return bookingRepository.findAllByBookerIdAndStatusOrderByIdDesc(bookerId, BookingStatus.valueOf(status));
    }

    public List<Booking> getAllByBooker(long bookerId) {
        return bookingRepository.findAllByBookerIdOrderByIdDesc(bookerId);
    }

    public List<Booking> getAllByItemOwnerIdWithStatus(long itemOwnerId, String status) {
        User itemOwner = userService.getUserById(itemOwnerId);

        return bookingRepository.findAllBookingsByItemOwnerWithStatus(itemOwner, BookingStatus.valueOf(status));
    }

    public List<Booking> getAllByItemOwnerId(long itemOwnerId) {
        User itemOwner = userService.getUserById(itemOwnerId);

        return bookingRepository.findAllBookingsByItemOwner(itemOwner);
    }
}
