package ru.practicum.shareit.item;

import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

public class ItemMapper {

    public static ItemDto toItemDto(Item item) {
        if (item == null) return null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequestId())
                .build();
    }

    public static ItemDto toItemDtoWithBookings(Item item,
                                                BookingShortDto lastBooking,
                                                BookingShortDto nextBooking,
                                                List<CommentDto> comments) {
        ItemDto dto = toItemDto(item);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static Item toItem(ItemDto itemDto, User owner) {
        if (itemDto == null) return null;

        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        item.setRequestId(itemDto.getRequestId());
        return item;
    }
}