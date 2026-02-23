package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserDtoResponse;
import ru.practicum.shareit.user.dto.UserDtoUpdate;
import ru.practicum.shareit.user.dto.UserListDto;

public interface UserService {

    UserDtoResponse createUser(UserDto userDto);

    UserDtoResponse getUserById(Long userId);

    UserListDto getUsers();

    UserDtoResponse updateUser(UserDtoUpdate userDto, Long userId);

    void deleteUser(Long userId);
}