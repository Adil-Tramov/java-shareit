package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.service.ItemService;
import ru.yandex.practicum.shareit.user.dto.UserDto;
import ru.yandex.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = userService.createUser(UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build());

        booker = userService.createUser(UserDto.builder()
                .name("Booker")
                .email("booker@example.com")
                .build());

        item = itemService.createItem(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build());
    }

    @Test
    void createItem_ShouldSaveToDatabase() {
        assertNotNull(item.getId());
        assertEquals("Drill", item.getName());
        assertEquals("Powerful drill", item.getDescription());
        assertTrue(item.getAvailable());
        assertEquals(owner.getId(), item.getOwnerId());
    }

    @Test
    void getItemById_ShouldReturnItem() {
        ItemDto foundItem = itemService.getItemById(owner.getId(), item.getId());

        assertNotNull(foundItem);
        assertEquals(item.getId(), foundItem.getId());
        assertEquals("Drill", foundItem.getName());
    }

    @Test
    void getUserItems_ShouldReturnOwnerItems() {
        List<ItemDto> items = itemService.getUserItems(owner.getId());

        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void updateItem_ShouldUpdateItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        ItemDto updatedItem = itemService.updateItem(owner.getId(), item.getId(), updateDto);

        assertEquals("Updated Drill", updatedItem.getName());
        assertEquals("Updated description", updatedItem.getDescription());
        assertFalse(updatedItem.getAvailable());
    }

    @Test
    void updateItem_WithNonOwner_ShouldThrowException() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .build();

        assertThrows(NotFoundException.class,
                () -> itemService.updateItem(booker.getId(), item.getId(), updateDto));
    }

    @Test
    void searchItems_ShouldReturnAvailableItems() {
        List<ItemDto> foundItems = itemService.searchItems("drill");

        assertNotNull(foundItems);
        assertEquals(1, foundItems.size());
        assertEquals("Drill", foundItems.get(0).getName());
    }

    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> foundItems = itemService.searchItems("");

        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void searchItems_WithNoMatches_ShouldReturnEmptyList() {
        List<ItemDto> foundItems = itemService.searchItems("nonexistent");

        assertNotNull(foundItems);
        assertTrue(foundItems.isEmpty());
    }

    @Test
    void addComment_ShouldAddComment() {
        // Этот тест требует наличия BookingService
        // Пропускаем или реализуем позже
    }
}