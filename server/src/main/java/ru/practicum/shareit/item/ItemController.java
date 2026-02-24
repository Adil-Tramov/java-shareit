package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        log.info("POST /items from user {}", userId);
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @PathVariable("itemId") Long itemId,
                              @RequestBody ItemDto itemDto) {
        log.info("PATCH /items/{} from user {}", itemId, userId);
        return itemService.updateItem(userId, itemId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingsDto getItemById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                           @PathVariable("itemId") Long itemId) {
        log.info("GET /items/{} from user {}", itemId, userId);
        return itemService.getItemById(userId, itemId);
    }

    @GetMapping
    public List<ItemWithBookingsDto> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("GET /items from user {}", userId);
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam("text") String text) {
        log.info("GET /items/search?text={}", text);
        return itemService.searchItems(text);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @PathVariable("itemId") Long itemId,
                                 @RequestBody CommentRequestDto commentRequestDto) {
        log.info("POST /items/{}/comment from user {}", itemId, userId);
        return itemService.addComment(userId, itemId, commentRequestDto);
    }
}