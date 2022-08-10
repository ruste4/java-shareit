package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.Generators;
import ru.practicum.shareit.booking.dto.BookingCreateDto;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private BookingService bookingService;

    @Autowired
    private MockMvc mvc;

    @Test
    void addBooking() throws Exception {
        Item item = Generators.ITEM_SUPPLIER.get();
        item.setId(1L);
        User booker = Generators.USER_SUPPLIER.get();
        booker.setId(2L);

        BookingCreateDto bookingCreateDto = BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();

        Booking booking = new Booking(
                1L,
                bookingCreateDto.getStart(),
                bookingCreateDto.getEnd(),
                item,
                booker,
                BookingStatus.WAITING
        );

        Mockito
                .when(bookingService.addBooking(bookingCreateDto, booker.getId()))
                .thenReturn(booking);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingCreateDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-Sharer-User-Id", booker.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(booking.getId().intValue())))
                .andExpect(jsonPath("$.booker.id", is(booker.getId().intValue())))
                .andExpect(jsonPath("$.booker.name", is(booker.getName())))
                .andExpect(jsonPath("$.status", is(booking.getStatus().toString())));
    }

    @Test
    void getAllBookingsCurrentUser() throws Exception {
        Item item1 = Generators.ITEM_SUPPLIER.get();
        item1.setId(1L);
        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setId(2L);

        User booker = Generators.USER_SUPPLIER.get();
        booker.setId(2L);

        Booking booking1 = new Booking(
                1L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item1,
                booker,
                BookingStatus.WAITING
        );

        Booking booking2 = new Booking(
                2L,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2),
                item2,
                booker,
                BookingStatus.WAITING
        );

        Mockito
                .when(bookingService.getAllBookingsCurrentUser(3, "ALL"))
                .thenReturn(List.of(booking1, booking2));

        mvc.perform(get("/bookings")
                .characterEncoding(StandardCharsets.UTF_8)
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Sharer-User-Id", 4))
                .andExpect(status().isOk());
    }

    @Test
    void getAllBookingsForItemsOwner() {
    }

    @Test
    void getBookingById() {
    }

    @Test
    void approveBooking() {
    }
}