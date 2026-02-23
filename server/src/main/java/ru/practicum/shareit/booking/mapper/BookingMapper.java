package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    Booking mapToBookingFromBookingDto(BookingDto bookingDto);

    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    BookingDtoResponse mapToBookingDtoResponse(Booking booking);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    ItemShortDto mapToItemShortDtoFromItem(Item item);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    UserShortDto mapToUserShortDtoFromUser(User user);
}