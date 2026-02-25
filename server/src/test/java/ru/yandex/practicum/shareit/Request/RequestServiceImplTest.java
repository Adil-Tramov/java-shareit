package ru.yandex.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.mapper.ItemMapper;
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

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private RequestServiceImpl requestService;

    private User user;
    private User anotherUser;
    private ItemRequest request;
    private ItemRequestDto requestDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        user = User.builder()
                .id(1L)
                .name("User")
                .email("user@example.com")
                .build();

        anotherUser = User.builder()
                .id(2L)
                .name("Another User")
                .email("another@example.com")
                .build();

        request = ItemRequest.builder()
                .id(1L)
                .description("Need a drill")
                .requestor(user)
                .created(now)
                .build();

        requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(now)
                .items(List.of())
                .build();
    }

    @Test
    void getRequestById_WithValidId_ShouldReturnRequest() {
        // Метод getRequestById НЕ вызывает userRepository.findById()
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of());

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(requestRepository).findById(1L);
        verify(itemRepository).findAllByRequestId(1L);
        verify(userRepository, never()).findById(anyLong()); // Убеждаемся, что не вызывается
    }

    @Test
    void getRequestById_WithInvalidRequestId_ShouldThrowNotFoundException() {
        // Метод getRequestById НЕ вызывает userRepository.findById()
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getRequestById(1L, 99L));
        verify(requestRepository).findById(99L);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getRequestById_WithInvalidUserId_ShouldStillWork() {
        // Метод getRequestById не проверяет существование пользователя!
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of());

        // Даже с несуществующим userId метод должен работать
        ItemRequestDto result = requestService.getRequestById(99L, 1L);

        assertNotNull(result);
        verify(requestRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
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
        verify(userRepository).findById(1L);
        verify(requestRepository).findByRequestorIdOrderByCreatedDesc(1L);
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getUserRequests_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getUserRequests(99L));
        verify(userRepository).findById(99L);
        verify(requestRepository, never()).findByRequestorIdOrderByCreatedDesc(anyLong());
    }

    @Test
    void getAllRequests_ShouldReturnList() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(anotherUser));
        when(requestRepository.findAllExceptUser(eq(2L), any(Pageable.class))).thenReturn(List.of(request));
        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(requestDto);
        when(itemRepository.findAllByRequestIdIn(anyList())).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.getAllRequests(2L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findById(2L);
        verify(requestRepository).findAllExceptUser(eq(2L), any(Pageable.class));
        verify(itemRepository).findAllByRequestIdIn(anyList());
    }

    @Test
    void getAllRequests_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.getAllRequests(99L, 0, 10));
        verify(userRepository).findById(99L);
        verify(requestRepository, never()).findAllExceptUser(anyLong(), any(Pageable.class));
    }

    @Test
    void createRequest_WithValidData_ShouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(requestMapper.toItemRequestDto(any(ItemRequest.class))).thenReturn(requestDto);

        ItemRequestDto result = requestService.createRequest(1L, "Need a drill");

        assertNotNull(result);
        assertEquals("Need a drill", result.getDescription());
        verify(userRepository).findById(1L);
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void createRequest_WithInvalidUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> requestService.createRequest(99L, "Need a drill"));
        verify(userRepository).findById(99L);
        verify(requestRepository, never()).save(any(ItemRequest.class));
    }

    @Test
    void getRequestById_WithItems_ShouldIncludeItems() {
        ItemDto itemDto = ItemDto.builder().id(1L).name("Drill").build();

        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(requestMapper.toItemRequestDto(request)).thenReturn(requestDto);
        when(itemRepository.findAllByRequestId(1L)).thenReturn(List.of());
        // Не мокаем itemMapper, так как он не используется в методе

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertNotNull(result);
        verify(requestRepository).findById(1L);
        verify(itemRepository).findAllByRequestId(1L);
        verify(userRepository, never()).findById(anyLong());
    }
}