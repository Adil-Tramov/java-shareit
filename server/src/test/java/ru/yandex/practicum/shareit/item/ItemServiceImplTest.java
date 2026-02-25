package ru.yandex.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.shareit.booking.mapper.BookingMapper;
import ru.yandex.practicum.shareit.booking.repository.BookingRepository;
import ru.yandex.practicum.shareit.exception.BadRequestException;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.CommentDto;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.mapper.ItemMapper;
import ru.yandex.practicum.shareit.item.model.Comment;
import ru.yandex.practicum.shareit.item.model.Item;
import ru.yandex.practicum.shareit.item.repository.CommentRepository;
import ru.yandex.practicum.shareit.item.repository.ItemRepository;
import ru.yandex.practicum.shareit.item.service.ItemServiceImpl;
import ru.yandex.practicum.shareit.request.repository.RequestRepository;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private Item item;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .name("Owner")
                .email("owner@example.com")
                .build();

        item = Item.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .owner(owner)
                .build();

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .ownerId(1L)
                .build();
    }

    @Test
    void getUserItems_ShouldReturnList() {
        when(itemRepository.findByOwnerIdOrderById(1L)).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);
        when(commentRepository.findAllByItemIdIn(anyList())).thenReturn(List.of());

        List<ItemDto> result = itemService.getUserItems(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Drill", result.get(0).getName());
    }

    @Test
    void getItemById_WithValidId_ShouldReturnItem() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);
        when(commentRepository.findAllByItemId(1L)).thenReturn(List.of());

        ItemDto result = itemService.getItemById(1L, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getItemById_WithInvalidId_ShouldThrowNotFoundException() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getItemById(1L, 99L));
    }

    @Test
    void searchItems_WithText_ShouldReturnList() {
        when(itemRepository.searchAvailableItems("drill")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        List<ItemDto> result = itemService.searchItems("drill");

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void searchItems_WithEmptyText_ShouldReturnEmptyList() {
        List<ItemDto> result = itemService.searchItems("");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(itemRepository, never()).searchAvailableItems(anyString());
    }

    @Test
    void createItem_WithValidData_ShouldCreateItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(any(ItemDto.class))).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertNotNull(result);
        assertEquals("Drill", result.getName());
    }

    @Test
    void createItem_WithInvalidOwner_ShouldThrowNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.createItem(99L, itemDto));
    }

    @Test
    void updateItem_WithValidData_ShouldUpdateItem() {
        ItemDto updateDto = ItemDto.builder()
                .name("Updated Drill")
                .description("Updated description")
                .available(false)
                .build();

        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(any(Item.class))).thenReturn(itemDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertNotNull(result);
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_WithNonOwner_ShouldThrowNotFoundException() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThrows(NotFoundException.class, () -> itemService.updateItem(2L, 1L, itemDto));
    }

    @Test
    void addComment_WithValidData_ShouldAddComment() {
        User booker = User.builder().id(2L).name("Booker").build();
        Comment comment = Comment.builder()
                .id(1L)
                .text("Great item")
                .item(item)
                .author(booker)
                .build();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item")
                .authorName("Booker")
                .build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItemIdAndEndBefore(eq(2L), eq(1L), any()))
                .thenReturn(List.of(mock(ru.yandex.practicum.shareit.booking.model.Booking.class)));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(itemMapper.toCommentDto(any(Comment.class))).thenReturn(commentDto);

        CommentDto result = itemService.addComment(2L, 1L, "Great item");

        assertNotNull(result);
        assertEquals("Great item", result.getText());
    }

    @Test
    void addComment_WithoutBooking_ShouldThrowBadRequestException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(mock(User.class)));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(), any()))
                .thenReturn(List.of());

        assertThrows(BadRequestException.class, () -> itemService.addComment(2L, 1L, "Great item"));
    }
}