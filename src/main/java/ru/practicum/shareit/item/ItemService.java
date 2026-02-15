package ru.practicum.shareit.item;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);
    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);
    ItemDto getItemById(Long itemId);
    List<ItemDto> getAllUserItems(Long userId);
    List<ItemDto> searchItems(String text);
}
