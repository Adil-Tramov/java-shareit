package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.client.BaseWebClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

@Service
public class UserClient extends BaseWebClient {
    private static final String API_PREFIX = "/users";


    public UserClient(@Value("${shareit-server.url}") String url) {
        super(WebClient.builder()
                .baseUrl(url + API_PREFIX)
                .build());
    }

    public Mono<ResponseEntity<Object>> createUser(UserDto userDto) {
        return post("", userDto);
    }

    public Mono<ResponseEntity<Object>> updateUser(UserDtoUpdate userDtoUpdate, Long userId) {
        return patch("/" + userId, userDtoUpdate);
    }

    public Mono<ResponseEntity<Object>> getUserById(Long userId) {
        return get("/" + userId);
    }

    public Mono<ResponseEntity<Object>> deleteUser(Long userId) {
        return delete("/" + userId);
    }

    public Mono<ResponseEntity<Object>> getUsers() {
        return get("");
    }
}