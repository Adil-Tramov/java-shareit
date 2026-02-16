package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        ItemDto createdItem = itemService.createItem(userId, itemDto);
        if (createdItem == null) {
            if (userService.getUserById(userId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID " + userId + " не найден");
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Некорректные данные предмета");
            }
        }
        return createdItem;
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable Long itemId,
                              @RequestBody ItemDto itemDto) {
        ItemDto updatedItem = itemService.updateItem(userId, itemId, itemDto);
        if (updatedItem == null) {
            ItemDto existingItem = itemService.getItemById(itemId);
            if (existingItem == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь с ID " + itemId + " не найдена");
            } else if (userService.getUserById(userId) == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID " + userId + " не найден");
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Пользователь с ID " + userId + " не является владельцем вещи");
            }
        }
        return updatedItem;
    }

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId) {
        ItemDto item = itemService.getItemById(itemId);
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Вещь с ID " + itemId + " не найдена");
        }
        return item;
    }

    @GetMapping
    public List<ItemDto> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        List<ItemDto> items = itemService.getAllUserItems(userId);
        if (items == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь с ID " + userId + " не найден");
        }
        return items;
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String text) {
        return itemService.searchItems(text);
    }
}