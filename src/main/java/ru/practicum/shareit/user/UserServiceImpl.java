package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Email не может быть пустым");
        }
        if (!email.contains("@")) {
            throw new ValidationException("Некорректный email");
        }
    }

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        validateEmail(userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());

            if (!userDto.getEmail().equals(existingUser.getEmail()) &&
                    userRepository.existsByEmail(userDto.getEmail())) {
                throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}