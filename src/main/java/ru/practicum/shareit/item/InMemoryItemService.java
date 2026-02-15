package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class InMemoryItemService implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);
    private final UserService userService;

    public InMemoryItemService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (userService.getUserById(userId) == null) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден");
        }
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new RuntimeException("Название вещи не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new RuntimeException("Описание вещи не может быть пустым");
        }
        Item item = ItemMapper.toItem(itemDto);
        item.setId(nextId.getAndIncrement());
        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);

        if (item.getAvailable() == null) {
            item.setAvailable(true);
        }
        items.put(item.getId(), item);
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = items.get(itemId);

        if (existingItem == null) {
            throw new RuntimeException("Вещь с ID " + itemId + " не найдена");
        }
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new RuntimeException("Пользователь с ID " + userId + " не является владельцем вещи с ID " + itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(existingItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new RuntimeException("Вещь с ID " + itemId + " не найдена");
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        if (userService.getUserById(userId) == null) {
            throw new RuntimeException("Пользователь с ID " + userId + " не найден");
        }

        return items.values().stream()
                .filter(item -> item.getOwner().getId().equals(userId))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String searchText = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable) // Только доступные вещи
                .filter(item -> item.getName().toLowerCase().contains(searchText) || item.getDescription().toLowerCase().contains(searchText))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
