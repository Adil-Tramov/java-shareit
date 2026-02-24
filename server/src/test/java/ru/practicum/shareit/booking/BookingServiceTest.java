package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private User wrongUser;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setName("Owner");
        owner.setEmail("owner@test.com");

        booker = new User();
        booker.setId(2L);
        booker.setName("Booker");
        booker.setEmail("booker@test.com");

        wrongUser = new User();
        wrongUser.setId(3L);
        wrongUser.setName("Wrong");
        wrongUser.setEmail("wrong@test.com");

        item = new Item();
        item.setId(1L);
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
    }

    @Test
    void approveBooking_ByOwner_ShouldSucceed() {
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(booking.getId())).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.approveBooking(owner.getId(), booking.getId(), true);

    }

    @Test
    void approveBooking_WithNonExistentUser_ShouldThrowNotFoundException() {
        Long nonExistentUserId = 999L;
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(nonExistentUserId, booking.getId(), true)
        );
    }

    @Test
    void approveBooking_WithNonExistentBooking_ShouldThrowNotFoundException() {
        Long nonExistentBookingId = 999L;
        when(userRepository.findById(owner.getId())).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(nonExistentBookingId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> bookingService.approveBooking(owner.getId(), nonExistentBookingId, true)
        );
    }
}