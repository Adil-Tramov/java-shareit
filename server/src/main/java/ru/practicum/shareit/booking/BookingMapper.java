package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.model.User;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        ItemDto itemDto = null;
        if (booking.getItem() != null) {
            itemDto = ItemDto.builder()
                    .id(booking.getItem().getId())
                    .name(booking.getItem().getName())
                    .description(booking.getItem().getDescription())
                    .available(booking.getItem().getAvailable())
                    .ownerId(booking.getItem().getOwner() != null ? booking.getItem().getOwner().getId() : null)
                    .requestId(booking.getItem().getRequestId())
                    .build();
        }

        UserDto userDto = null;
        if (booking.getBooker() != null) {
            userDto = UserDto.builder()
                    .id(booking.getBooker().getId())
                    .name(booking.getBooker().getName())
                    .email(booking.getBooker().getEmail())
                    .build();
        }

        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(itemDto)
                .booker(userDto)
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingRequestDto bookingRequestDto, Item item, User booker) {
        if (bookingRequestDto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        return booking;
    }
}