package ru.practicum.shareit.request;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(ItemRequest request) {
        if (request == null) return null;

        List<ItemDto> itemDtos = null;
        if (request.getItems() != null) {
            itemDtos = request.getItems().stream()
                    .map(ItemRequestMapper::toItemDto)
                    .collect(Collectors.toList());
        }

        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }

    private static ItemDto toItemDto(Item item) {
        if (item == null) return null;

        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner().getId())
                .requestId(item.getRequestId())
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto requestDto, User requestor) {
        if (requestDto == null) return null;

        ItemRequest request = new ItemRequest();
        request.setId(requestDto.getId());
        request.setDescription(requestDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(requestDto.getCreated() != null ? requestDto.getCreated() : LocalDateTime.now());
        return request;
    }
}