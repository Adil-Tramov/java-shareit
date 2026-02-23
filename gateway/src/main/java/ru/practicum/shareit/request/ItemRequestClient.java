package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.client.BaseWebClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Map;

@Service
public class ItemRequestClient extends BaseWebClient {
    private static final String API_PREFIX = "/requests";


    public ItemRequestClient(@Value("${shareit-server.url}") String url) {
        super(WebClient.builder()
                .baseUrl(url + API_PREFIX)
                .build());
    }

    public Mono<ResponseEntity<Object>> createRequest(Long requesterId, ItemRequestDto itemRequestDto) {
        return post("", requesterId, itemRequestDto);
    }

    public Mono<ResponseEntity<Object>> getPrivateRequests(Long requesterId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("?from={from}&&size={size}", requesterId, parameters);
    }

    public Mono<ResponseEntity<Object>> getOtherRequests(Long requesterId, Integer from, Integer size) {
        Map<String, Object> parameters = Map.of(
                "from", from,
                "size", size
        );
        return get("/all?from={from}&&size={size}", requesterId, parameters);
    }

    public Mono<ResponseEntity<Object>> getItemRequest(Long userId, Long requestId) {
        return get("/" + userId, requestId);
    }
}