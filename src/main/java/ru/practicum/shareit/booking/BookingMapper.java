package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) return null;

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(BookingDto.BookingItemDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .booker(BookingDto.BookingUserDto.builder()
                        .id(booking.getBooker().getId())
                        .name(booking.getBooker().getName())
                        .build())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto, Item item, User booker) {
        if (bookingDto == null) return null;

        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }
}