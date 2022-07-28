package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Generators;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.exceptions.BookingIncorrectStartEndDatesException;
import ru.practicum.shareit.booking.exceptions.BookingNotFound;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceTest {
    @Autowired
    private final BookingService bookingService;

    @Autowired
    private final TestEntityManager testEntityManager;

    @BeforeEach
    public void beforeEachItemServiceTest() {
        testEntityManager.clear();
    }

    @Test
    public void bookingCreateFromUser1ToItem2Current() {
        User user = Generators.USER_SUPPLIER.get();
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(booking.getItem(), Long.class);
        Long bookerId = testEntityManager.persistAndGetId(user, Long.class);
        testEntityManager.flush();

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();

        Booking addedBooking = bookingService.addBooking(bookingCreateDto, bookerId);

        Booking foundBooking = testEntityManager.find(Booking.class, addedBooking.getId());

        assertEquals(addedBooking, foundBooking);
    }

    @Test
    public void bookingSetApproveAndUnavailableByOwnerCurrent() {
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        testEntityManager.persist(booking.getItem());
        testEntityManager.persist(booking.getBooker());
        Long bookingId = testEntityManager.persistAndGetId(booking, Long.class);
        testEntityManager.flush();

        assertAll(
                () -> assertNotEquals(
                        testEntityManager.find(Booking.class, bookingId).getStatus(), BookingStatus.APPROVED
                ),
                () -> assertDoesNotThrow(
                        () -> bookingService.approveBooking(bookingId, ownerId, true), "Set approve"
                ),
                () -> assertEquals(
                        testEntityManager.find(Booking.class, bookingId).getStatus(), BookingStatus.APPROVED
                ),
                () -> assertDoesNotThrow(
                        () -> bookingService.approveBooking(bookingId, ownerId, false), "Set approve"
                ),
                () -> assertNotEquals(
                        testEntityManager.find(Booking.class, bookingId).getStatus(), BookingStatus.APPROVED
                )
        );
    }

    @Test
    public void bookingCreateFailedByWrongUserId() {
        User user = Generators.USER_SUPPLIER.get();
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(booking.getItem(), Long.class);
        testEntityManager.persist(user);
        testEntityManager.flush();

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();

        assertThrows(UserNotFoundException.class, () -> bookingService.addBooking(bookingCreateDto, 100));
    }

    @Test
    public void bookingCreateFailedByNotFoundItemId() {
        User user = Generators.USER_SUPPLIER.get();
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        testEntityManager.persist(booking.getItem());
        Long bookerId = testEntityManager.persistAndGetId(user, Long.class);
        testEntityManager.flush();

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(100L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();

        assertThrows(ItemNotFoundException.class, () -> bookingService.addBooking(bookingCreateDto, bookerId));
    }

    @Test
    public void startEndBookingValidation() {
        User booker = Generators.USER_SUPPLIER.get();
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(booking.getItem(), Long.class);
        Long bookerId = testEntityManager.persistAndGetId(booker, Long.class);
        testEntityManager.flush();

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(itemId)
                .start(booking.getStart())
                .end(booking.getEnd())
                .build();

        assertAll(
                () -> assertThrows(
                        BookingIncorrectStartEndDatesException.class,
                        () -> {
                            bookingCreateDto.setEnd(LocalDateTime.now().minusDays(1));
                            bookingService.addBooking(bookingCreateDto, bookerId);
                        }, "Booking create failed by end in past"
                ),
                () -> assertThrows(
                        BookingIncorrectStartEndDatesException.class,
                        () -> {
                            booking.setStart(LocalDateTime.now().plusDays(1));
                            booking.setEnd(LocalDateTime.now().plusHours(1));
                            bookingService.addBooking(bookingCreateDto, bookerId);
                        }, "Booking create failed by end before start"
                ),
                () -> assertThrows(
                        BookingIncorrectStartEndDatesException.class,
                        () -> {
                            booking.setStart(LocalDateTime.now().minusDays(1));
                            booking.setEnd(LocalDateTime.now().plusDays(2));
                            bookingService.addBooking(bookingCreateDto, bookerId);
                        }, "Booking create failed by start in past"
                )
        );
    }

    @Test
    public void bookingGet() {
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persist(booking.getItem().getOwner());
        testEntityManager.persist(booking.getItem());
        Long bookerId = testEntityManager.persistAndGetId(booking.getBooker(), Long.class);
        Long bookingId = testEntityManager.persistAndGetId(booking, Long.class);
        testEntityManager.flush();

        Booking bookingFromService = bookingService.getBookingById(bookingId, bookerId);
        Booking foundedBooking = testEntityManager.find(Booking.class, bookingId);

        assertEquals(bookingFromService, foundedBooking);
    }

    @Test
    public void bookingGetByOwner() {
        Booking booking = Generators.BOOKING_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(booking.getItem().getOwner(), Long.class);
        testEntityManager.persist(booking.getItem());
        testEntityManager.persist(booking.getBooker());
        Long bookingId = testEntityManager.persistAndGetId(booking, Long.class);
        testEntityManager.flush();

        List<Booking> bookingsFromService = bookingService.getAllByItemOwnerId(ownerId);
        Booking foundedBooking = testEntityManager.find(Booking.class, bookingId);

        assertEquals(bookingsFromService.get(0), foundedBooking);
    }

    @Test
    public void bookingGetAllForUser() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persist(booking1.getItem().getOwner());
        testEntityManager.persist(booking1.getItem());
        Long bookerId = testEntityManager.persistAndGetId(booking1.getBooker(), Long.class);
        testEntityManager.persist(booking1);

        Booking booking2 = Generators.BOOKING_SUPPLIER.get();
        booking2.setBooker(booking1.getBooker());
        testEntityManager.persist(booking2.getItem().getOwner());
        testEntityManager.persist(booking2.getItem());
        testEntityManager.persist(booking2.getBooker());
        testEntityManager.persist(booking2);

        testEntityManager.flush();

        assertEquals(bookingService.getAllByBooker(bookerId).size(), 2);
    }

    @Test
    public void bookingGetAllForOwner() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        User owner = testEntityManager.persist(booking1.getItem().getOwner());
        testEntityManager.persist(booking1.getItem());
        testEntityManager.persist(booking1.getBooker());
        testEntityManager.persist(booking1);

        Booking booking2 = Generators.BOOKING_SUPPLIER.get();
        booking2.getItem().setOwner(owner);
        testEntityManager.persist(booking2.getItem().getOwner());
        testEntityManager.persist(booking2.getItem());
        testEntityManager.persist(booking2.getBooker());
        testEntityManager.persist(booking2);

        testEntityManager.flush();

        assertEquals(bookingService.getAllByItemOwnerId(owner.getId()).size(), 2);
    }

    @Test
    public void bookingGetUnkonwn() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        User owner = testEntityManager.persist(booking1.getItem().getOwner());
        testEntityManager.persist(booking1.getItem());
        testEntityManager.persist(booking1.getBooker());
        testEntityManager.persist(booking1);

        assertThrows(BookingNotFound.class, () -> bookingService.getBookingById(100L, owner.getId()));
    }

    @Test
    public void bookingGetFromOtherUser() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persist(booking1.getItem().getOwner());
        testEntityManager.persist(booking1.getItem());
        testEntityManager.persist(booking1.getBooker());
        Long bookingId = testEntityManager.persistAndGetId(booking1, Long.class);

        assertThrows(UserNotFoundException.class, () -> bookingService.getBookingById(bookingId, 100L));
    }

    @Test
    public void bookingChangeStatusByBooker() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        testEntityManager.persist(booking1.getItem().getOwner());
        testEntityManager.persist(booking1.getItem());
        Long bookerId = testEntityManager.persistAndGetId(booking1.getBooker(), Long.class);
        Long bookingId = testEntityManager.persistAndGetId(booking1, Long.class);

        assertThrows(
                UserNotOwnerItemException.class, () -> bookingService.approveBooking(bookingId, bookerId, true)
        );
    }

    @Test
    public void bookingSetApproveByOwner() {
        Booking booking1 = Generators.BOOKING_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(booking1.getItem().getOwner(), Long.class);
        testEntityManager.persist(booking1.getItem());
        testEntityManager.persist(booking1.getBooker());
        Long bookingId = testEntityManager.persistAndGetId(booking1, Long.class);

        assertDoesNotThrow(() -> bookingService.approveBooking(bookingId, ownerId, true));
    }
}