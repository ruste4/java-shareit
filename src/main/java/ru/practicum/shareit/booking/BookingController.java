package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public BookingDto addBooking(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody BookingCreateDto bookingCreateDto
    ) {
        return BookingMapper.toBookingDto(bookingService.addBooking(bookingCreateDto, userId));
    }

    @GetMapping
    public List<BookingDto> getAllBookingsCurrentUser(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "100") @Min(1) Integer size
    ) {
        return bookingService.getAllBookingsCurrentUser(userId, state, from, size).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingsForItemsOwner(
            @RequestHeader("X-Sharer-User-Id") long itemOwnerId,
            @RequestParam(defaultValue = "All") String state,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "100") @Min(1) Integer size
    ) {
        return bookingService.getAllBookingsForItemsOwner(itemOwnerId, state, from, size).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public BookingDto getBookingById(
            @PathVariable long id,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        return BookingMapper.toBookingDto(
                bookingService.getBookingById(id, userId)
        );
    }

    @PatchMapping("/{id}")
    public BookingDto approveBooking(
            @PathVariable long id,
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam boolean approved
    ) {
        return BookingMapper.toBookingDto(
                bookingService.approveBooking(id, userId, approved)
        );
    }

}
