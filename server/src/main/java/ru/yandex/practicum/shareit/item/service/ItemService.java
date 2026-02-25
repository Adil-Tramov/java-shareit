package ru.yandex.practicum.shareit.item.service;

import ru.yandex.practicum.shareit.item.dto.CommentDto;
import ru.yandex.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getUserItems(Long userId);

    ItemDto getItemById(Long userId, Long itemId);

    List<ItemDto> searchItems(String text);

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    CommentDto addComment(Long userId, Long itemId, String text);
}