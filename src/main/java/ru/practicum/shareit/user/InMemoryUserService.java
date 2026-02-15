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
            return null;
        }

        // Проверка email при обновлении
        if (userDto.getEmail() != null && !userDto.getEmail().equals(existingUser.getEmail())) {
            if (users.values().stream()
                    .filter(u -> !u.getId().equals(userId))
                    .anyMatch(u -> u.getEmail().equals(userDto.getEmail()))) {
                return null;
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        return UserMapper.toUserDto(existingUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = users.get(userId);
        return user != null ? UserMapper.toUserDto(user) : null;
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }
}

