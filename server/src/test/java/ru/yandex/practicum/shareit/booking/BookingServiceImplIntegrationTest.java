package ru.yandex.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.dto.BookingDto;
import ru.yandex.practicum.shareit.booking.model.BookingStatus;
import ru.yandex.practicum.shareit.booking.service.BookingService;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.service.ItemService;
import ru.yandex.practicum.shareit.user.dto.UserDto;
import ru.yandex.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userService.createUser(UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build());
    }

    @Test
    void createBooking_ShouldSaveToDatabase() {
        BookingDto booking = bookingService.createBooking(
                booker.getId(),
                item.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );

        assertNotNull(booking.getId());
        assertEquals(BookingStatus.WAITING, booking.getStatus());
        assertEquals(booker.getId(), booking.getBooker().getId());
        assertEquals(item.getId(), booking.getItem().getId());
    }

    @Test
    void getBookingById_ShouldReturnBooking() {
        BookingDto savedBooking = bookingService.createBooking(
                booker.getId(),
                item.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );

        BookingDto foundBooking = bookingService.getBookingById(booker.getId(), savedBooking.getId());

        assertNotNull(foundBooking);
        assertEquals(savedBooking.getId(), foundBooking.getId());
    }

    @Test
    void getUserBookings_ShouldReturnUserBookings() {
        bookingService.createBooking(booker.getId(), item.getId(), now.plusDays(1), now.plusDays(2));
        bookingService.createBooking(booker.getId(), item.getId(), now.plusDays(3), now.plusDays(4));

        List<BookingDto> bookings = bookingService.getUserBookings(booker.getId(), "ALL", 0, 10);

        assertEquals(2, bookings.size());
    }

    @Test
    void getOwnerBookings_ShouldReturnOwnerBookings() {
        bookingService.createBooking(booker.getId(), item.getId(), now.plusDays(1), now.plusDays(2));

        List<BookingDto> bookings = bookingService.getOwnerBookings(owner.getId(), "ALL", 0, 10);

        assertEquals(1, bookings.size());
    }

    @Test
    void approveBooking_ShouldUpdateStatus() {
        BookingDto booking = bookingService.createBooking(
                booker.getId(),
                item.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );

        BookingDto approvedBooking = bookingService.approveBooking(owner.getId(), booking.getId(), true);

        assertEquals(BookingStatus.APPROVED, approvedBooking.getStatus());
    }

    @Test
    void rejectBooking_ShouldUpdateStatus() {
        BookingDto booking = bookingService.createBooking(
                booker.getId(),
                item.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );

        BookingDto rejectedBooking = bookingService.approveBooking(owner.getId(), booking.getId(), false);

        assertEquals(BookingStatus.REJECTED, rejectedBooking.getStatus());
    }

    @Test
    void approveBooking_WithNonOwner_ShouldThrowException() {
        BookingDto booking = bookingService.createBooking(
                booker.getId(),
                item.getId(),
                now.plusDays(1),
                now.plusDays(2)
        );

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(99L, booking.getId(), true));
    }
}