package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.item.service.ItemService;
import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;
import ru.yandex.practicum.shareit.request.service.RequestService;
import ru.yandex.practicum.shareit.user.dto.UserDto;
import ru.yandex.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/schema.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class RequestServiceImplIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private UserService userService;

    @Autowired
    private ItemService itemService;

    private UserDto user1;
    private UserDto user2;

    @BeforeEach
    void setUp() {
        user1 = userService.createUser(UserDto.builder()
                .name("User 1")
                .email("user1@example.com")
                .build());

        user2 = userService.createUser(UserDto.builder()
                .name("User 2")
                .email("user2@example.com")
                .build());
    }

    @Test
    void createRequest_ShouldSaveToDatabase() {
        ItemRequestDto request = requestService.createRequest(user1.getId(), "Need a drill");

        assertNotNull(request.getId());
        assertEquals("Need a drill", request.getDescription());
        assertEquals(user1.getId(), request.getRequestorId());
    }

    @Test
    void getRequestById_ShouldReturnRequest() {
        ItemRequestDto savedRequest = requestService.createRequest(user1.getId(), "Need a drill");

        ItemRequestDto foundRequest = requestService.getRequestById(user2.getId(), savedRequest.getId());

        assertNotNull(foundRequest);
        assertEquals(savedRequest.getId(), foundRequest.getId());
        assertEquals("Need a drill", foundRequest.getDescription());
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        requestService.createRequest(user1.getId(), "Request 1");
        requestService.createRequest(user1.getId(), "Request 2");

        List<ItemRequestDto> requests = requestService.getUserRequests(user1.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        requestService.createRequest(user1.getId(), "Request from user1");
        requestService.createRequest(user1.getId(), "Another request from user1");

        List<ItemRequestDto> requests = requestService.getAllRequests(user2.getId(), 0, 10);

        assertEquals(2, requests.size());
    }

    @Test
    void getRequestById_WithItems_ShouldIncludeItems() {
        ItemRequestDto request = requestService.createRequest(user1.getId(), "Need a drill");

        itemService.createItem(user2.getId(), ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .requestId(request.getId())
                .build());

        ItemRequestDto foundRequest = requestService.getRequestById(user2.getId(), request.getId());

        assertNotNull(foundRequest.getItems());
        assertEquals(1, foundRequest.getItems().size());
        assertEquals("Drill", foundRequest.getItems().get(0).getName());
    }
}