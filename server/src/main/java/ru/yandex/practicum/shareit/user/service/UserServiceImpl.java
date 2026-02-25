package ru.yandex.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.exception.DuplicateEmailException;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.user.dto.UserDto;
import ru.yandex.practicum.shareit.user.mapper.UserMapper;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Getting user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Creating user: {}", userDto);

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateEmailException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = userMapper.toUser(userDto);
        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Updating user {} with data: {}", id, userDto);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new DuplicateEmailException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }
            user.setEmail(userDto.getEmail());
        }

        user = userRepository.save(user);
        return userMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        userRepository.deleteById(id);
    }
}