package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InMemoryUserService implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public UserDto createUser(UserDto userDto) {
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
            System.err.println("Пользователь с таким email уже существует: " + userDto.getEmail());
            return null;
        }

        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = users.get(userId);
        if (existingUser == null) {
            System.err.println("Пользователь с ID " + userId + " не найден.");
            return null;
        }
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (users.values().stream()
                    .filter(u -> !u.getId().equals(userId))
                    .anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                System.err.println("Пользователь с таким email уже существует: " + userDto.getEmail());
                return null;
            }
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            System.err.println("Пользователь с ID " + userId + " не найден.");
            return null;
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        User removedUser = users.remove(userId);
        if (removedUser == null) {
            System.err.println("Пользователь с ID " + userId + " не найден для удаления.");
        }
    }
}

