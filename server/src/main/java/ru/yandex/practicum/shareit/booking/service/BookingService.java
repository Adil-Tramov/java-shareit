package ru.yandex.practicum.shareit.booking.service;

import ru.yandex.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    BookingDto getBookingById(Long userId, Long bookingId);

    List<BookingDto> getUserBookings(Long userId, String state, int from, int size);

    List<BookingDto> getOwnerBookings(Long userId, String state, int from, int size);

    BookingDto createBooking(Long userId, Long itemId, LocalDateTime start, LocalDateTime end);

    BookingDto approveBooking(Long userId, Long bookingId, boolean approved);
}