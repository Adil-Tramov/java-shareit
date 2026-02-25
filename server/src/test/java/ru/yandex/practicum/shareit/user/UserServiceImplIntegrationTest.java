package ru.yandex.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.exception.DuplicateEmailException;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.user.dto.UserDto;
import ru.yandex.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void createUser_ShouldSaveUserToDatabase() {
        UserDto savedUser = userService.createUser(userDto);

        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john@example.com", savedUser.getEmail());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldThrowException() {
        userService.createUser(userDto);

        UserDto duplicateUser = UserDto.builder()
                .name("Jane Doe")
                .email("john@example.com")
                .build();

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(duplicateUser));
    }

    @Test
    void getUserById_ShouldReturnUser() {
        UserDto savedUser = userService.createUser(userDto);

        UserDto foundUser = userService.getUserById(savedUser.getId());

        assertNotNull(foundUser);
        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals(savedUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void getUserById_WithInvalidId_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser(userDto);
        userService.createUser(UserDto.builder().name("Jane Doe").email("jane@example.com").build());

        List<UserDto> users = userService.getAllUsers();

        assertEquals(2, users.size());
    }

    @Test
    void updateUser_ShouldUpdateUser() {
        UserDto savedUser = userService.createUser(userDto);

        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("updated@example.com")
                .build();

        UserDto updatedUser = userService.updateUser(savedUser.getId(), updateDto);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        UserDto savedUser = userService.createUser(userDto);

        userService.deleteUser(savedUser.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(savedUser.getId()));
    }
}