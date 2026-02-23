package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoUpdate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/users")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController {
    private final UserClient userClient;

    @PostMapping
    public Mono<ResponseEntity<Object>> createUser(@Valid @RequestBody UserDto userDto) {
        return userClient.createUser(userDto);
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<Object>> getUserById(@PathVariable("id") @Min(1) Long userId) {
        return userClient.getUserById(userId);
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> getUsers() {
        return userClient.getUsers();
    }

    @PatchMapping("{id}")
    public Mono<ResponseEntity<Object>> updateUser(
            @RequestBody UserDtoUpdate userDtoUpdate,
            @PathVariable("id") Long userId) {
        return userClient.updateUser(userDtoUpdate, userId);
    }

    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Object>> deleteUser(@Min(1) @PathVariable("id") Long userId) {
        return userClient.deleteUser(userId);
    }
}