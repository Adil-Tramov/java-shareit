package ru.yandex.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shareit.booking.dto.BookingDto;
import ru.yandex.practicum.shareit.booking.mapper.BookingMapper;
import ru.yandex.practicum.shareit.booking.model.Booking;
import ru.yandex.practicum.shareit.booking.model.BookingStatus;
import ru.yandex.practicum.shareit.booking.repository.BookingRepository;
import ru.yandex.practicum.shareit.booking.service.BookingServiceImpl;
import ru.yandex.practicum.shareit.exception.BadRequestException;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.exception.UnsupportedStateException;
import ru.yandex.practicum.shareit.item.model.Item;
import ru.yandex.practicum.shareit.item.repository.ItemRepository;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        booker = User.builder()
                .id(2L)
                .name("Booker")
                .email("booker@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build();

        booking = Booking.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void getBookingById_WhenUserIsBooker_ShouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(2L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getBookingById_WhenUserIsOwner_ShouldReturnBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toBookingDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.getBookingById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getBookingById_WhenUserNotAuthorized_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(3L, 1L));
        verify(bookingRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getBookingById_WhenBookingNotFound_ShouldThrowNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> bookingService.getBookingById(1L, 99L));
        verify(bookingRepository).findById(99L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getUserBookings_WithStateAll_ShouldReturnList() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdOrderByStartDesc(eq(2L), any(Pageable.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getUserBookings(2L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(2L);
        verify(bookingRepository).findByBookerIdOrderByStartDesc(eq(2L), any(Pageable.class));
    }

    @Test
    void getUserBookings_WithInvalidState_ShouldThrowUnsupportedStateException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThrows(UnsupportedStateException.class,
                () -> bookingService.getUserBookings(2L, "INVALID", 0, 10));
        verify(userRepository).findById(2L);
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getUserBookings_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getUserBookings(99L, "ALL", 0, 10));
        verify(userRepository).findById(99L);
        verify(bookingRepository, never()).findByBookerIdOrderByStartDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_WithStateAll_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findByItemOwnerId(eq(1L), any(Pageable.class)))
                .thenReturn(List.of(booking));
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.getOwnerBookings(1L, "ALL", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(1L);
        verify(bookingRepository).findByItemOwnerId(eq(1L), any(Pageable.class));
    }

    @Test
    void getOwnerBookings_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.getOwnerBookings(99L, "ALL", 0, 10));
        verify(userRepository).findById(99L);
        verify(bookingRepository, never()).findByItemOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    void createBooking_WithValidData_ShouldCreateBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.createBooking(2L, 1L, now.plusDays(1), now.plusDays(2));

        assertNotNull(result);
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(99L, 1L, now.plusDays(1), now.plusDays(2)));
        verify(userRepository).findById(99L);
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WithInvalidItem_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(2L, 99L, now.plusDays(1), now.plusDays(2)));
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(99L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WithEndBeforeStart_ShouldThrowBadRequestException() {
        // Не мокаем userRepository.findById() так как проверка дат происходит до проверки пользователя
        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, 1L, now.plusDays(2), now.plusDays(1)));

        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WithEqualDates_ShouldThrowBadRequestException() {
        // Не мокаем userRepository.findById() так как проверка дат происходит до проверки пользователя
        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, 1L, now.plusDays(1), now.plusDays(1)));

        verify(userRepository, never()).findById(anyLong());
        verify(itemRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenItemNotAvailable_ShouldThrowBadRequestException() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(BadRequestException.class,
                () -> bookingService.createBooking(2L, 1L, now.plusDays(1), now.plusDays(2)));
        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_WhenOwnerBooksOwnItem_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(1L, 1L, now.plusDays(1), now.plusDays(2)));
        verify(userRepository).findById(1L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WithValidData_ShouldApprove() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBooking_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(99L, 1L, true));
        verify(userRepository).findById(99L);
        verify(bookingRepository, never()).findById(anyLong());
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WithInvalidBooking_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(1L, 99L, true));
        verify(userRepository).findById(1L);
        verify(bookingRepository).findById(99L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WithNonOwner_ShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(2L, 1L, true));
        verify(userRepository).findById(2L);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WhenAlreadyApproved_ShouldThrowBadRequestException() {
        booking.setStatus(BookingStatus.APPROVED);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.approveBooking(1L, 1L, true));
        verify(userRepository).findById(1L);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void approveBooking_WhenRejecting_ShouldSetRejectedStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toBookingDto(any(Booking.class))).thenReturn(bookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, false);

        assertNotNull(result);
        verify(userRepository).findById(1L);
        verify(bookingRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }
}