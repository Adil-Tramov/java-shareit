package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.dto.UserListDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final UserMapper mapper;

    @Override
    @Transactional
    public UserDtoResponse createUser(UserDto userDto) {
        if (users.existsByEmail(userDto.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    String.format("Пользователь с email %s уже существует", userDto.getEmail()));
        }

        User user = mapper.mapToUser(userDto);
        return mapper.mapToUserDtoResponse(users.save(user));
    }

    @Override
    @Transactional
    public UserDtoResponse updateUser(UserDtoUpdate userDto, Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Пользователь с id %d не найден", userId)));

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (users.existsByEmail(userDto.getEmail())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        String.format("Пользователь с email %s уже существует", userDto.getEmail()));
            }
        }

        User updatedUser = mapper.mapToUserFromUpdate(userDto, user);
        return mapper.mapToUserDtoResponse(users.save(updatedUser));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDtoResponse getUserById(Long userId) {
        User user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Пользователь с id %d не найден", userId)));
        return mapper.mapToUserDtoResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserListDto getUsers() {
        List<UserDtoResponse> userList = users.findAll().stream()
                .map(mapper::mapToUserDtoResponse)
                .collect(Collectors.toList());
        return UserListDto.builder().users(userList).build();
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id %d не найден", userId));
        }
        users.deleteById(userId);
    }
}