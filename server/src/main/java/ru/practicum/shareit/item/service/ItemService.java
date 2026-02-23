package ru.practicum.shareit.item.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.dto.ItemListDto;

public interface ItemService {

    ItemDtoResponse createItem(ItemDto itemDto, Long userId);

    ItemDtoResponse updateItem(Long itemId, Long userId, ItemDtoUpdate itemDto);

    ItemDtoResponse getItemByItemId(Long userId, Long itemId);

    ItemListDto getPersonalItems(Pageable pageable, Long userId);

    ItemListDto getFoundItems(Pageable pageable, String text);

    CommentDtoResponse addComment(Long itemId, Long userId, CommentDto commentDto);
}