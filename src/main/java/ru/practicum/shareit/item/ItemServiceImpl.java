package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserMapper;           // <-- ДОБАВЬТЕ ЭТОТ ИМПОРТ
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private boolean validateItemData(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            return false;
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            return false;
        }
        if (itemDto.getAvailable() == null) {
            return false;
        }
        return true;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        // 1. СНАЧАЛА проверяем существование пользователя
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 404 Not Found
        }

        // 2. ПОТОМ валидация данных
        if (!validateItemData(itemDto)) {
            return null; // 400 Bad Request
        }

        Item item = ItemMapper.toItem(itemDto);
        User owner = new User();
        owner.setId(userId);
        item.setOwner(owner);

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        // 1. СНАЧАЛА проверяем существование вещи
        Item existingItem = itemRepository.findById(itemId).orElse(null);
        if (existingItem == null) {
            return null; // 404 Not Found
        }

        // 2. ПОТОМ проверяем права доступа
        if (!existingItem.getOwner().getId().equals(userId)) {
            return null; // 403 Forbidden
        }

        // 3. ПОТОМ валидация данных при обновлении
        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            return null; // 400 Bad Request
        }
        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            return null; // 400 Bad Request
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {        // ЭТОТ МЕТОД ДОЛЖЕН БЫТЬ ТАКИМ
        Item item = itemRepository.findById(itemId).orElse(null);
        if (item == null) {
            return null; // 404 Not Found
        }
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        // Проверяем существование пользователя
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null; // 404 Not Found
        }

        return itemRepository.findAllByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}