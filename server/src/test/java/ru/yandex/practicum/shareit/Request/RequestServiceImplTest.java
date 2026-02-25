package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.repository.ItemRepository;
import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;
import ru.yandex.practicum.shareit.request.mapper.RequestMapper;
import ru.yandex.practicum.shareit.request.model.ItemRequest;
import ru.yandex.practicum.shareit.request.repository.RequestRepository;
import ru.yandex.practicum.shareit.request.service.RequestServiceImpl;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private ItemRequest request;
    private ItemRequestDto requestDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getRequestById_WithValidId_ShouldReturnRequest() {
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of());

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
    }

    @Test
    void getRequestById_WithInvalidId_ShouldThrowNotFoundException() {
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
    }

    @Test
    void getUserRequests_WithValidUser_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(requestDto);
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getUserRequests_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getUserRequests(99L));
    }

    @Test
    void getAllRequests_ShouldReturnList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllExceptUser(eq(1L), any(Pageable.class))).thenReturn(List.of(request));
        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(requestDto);
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void createRequest_WithValidData_ShouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        ItemRequestDto result = requestService.createRequest(1L, "Need a drill");

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.createRequest(99L, "Need a drill"));
    }
}
