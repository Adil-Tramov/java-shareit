package ru.yandex.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.shareit.booking.dto.BookingCreateDto;
import ru.yandex.practicum.shareit.booking.dto.BookingDto;
import ru.yandex.practicum.shareit.booking.dto.BookingShortDto;
import ru.yandex.practicum.shareit.booking.model.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingMapper INSTANCE = Mappers.getMapper(BookingMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    Booking toBooking(BookingCreateDto bookingCreateDto);

    @Mapping(target = "item.ownerId", ignore = true)
    @Mapping(target = "item.requestId", ignore = true)
    @Mapping(target = "item.lastBooking", ignore = true)
    @Mapping(target = "item.nextBooking", ignore = true)
    @Mapping(target = "item.comments", ignore = true)
    BookingDto toBookingDto(Booking booking);

    @Mapping(target = "bookerId", source = "booker.id")
    BookingShortDto toBookingShortDto(Booking booking);
}