package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.client.BaseWebClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;

import java.util.Map;

@Service
public class ItemClient extends BaseWebClient {
    private static final String API_PREFIX = "/items";


    public ItemClient(@Value("${shareit-server.url}") String url) {
        super(WebClient.builder()
                .baseUrl(url + API_PREFIX)
                .build());
    }

    public Mono<ResponseEntity<Object>> createItem(Long userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public Mono<ResponseEntity<Object>> updateItem(Long userId, ItemDtoUpdate itemDtoUpdate, Long itemId) {
        return patch("/" + itemId, userId, itemDtoUpdate);
    }

    public Mono<ResponseEntity<Object>> getItemByItemId(Long userId, Long itemId) {
        return get("/" + itemId, userId);
    }

    public Mono<ResponseEntity<Object>> getPersonalItems(Long userId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&size={size}", userId, parameters);
    }

    public Mono<ResponseEntity<Object>> getFoundItems(Long userId, String text, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "text", text,
                "from", from,
                "size", size
        );
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }

    public Mono<ResponseEntity<Object>> addComment(Long itemId, Long userId, CommentDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}