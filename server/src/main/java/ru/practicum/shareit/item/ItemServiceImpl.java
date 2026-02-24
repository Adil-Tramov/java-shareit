package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        validateItemData(itemDto);

        Item item = ItemMapper.toItem(itemDto, user);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с ID {}", savedItem.getId());

        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи {} пользователем {}", itemId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не является владельцем вещи");
        }

        // Обновление полей
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long userId, Long itemId) {
        log.info("Получение вещи {} пользователем {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (userId != null && item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();

            List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
            BookingShortDto lastBooking = null;
            if (!lastBookings.isEmpty()) {
                lastBooking = mapToBookingShortDto(lastBookings.get(0));
            }

            List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);
            BookingShortDto nextBooking = null;
            if (!nextBookings.isEmpty()) {
                nextBooking = mapToBookingShortDto(nextBookings.get(0));
            }

            return ItemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
        }

        return ItemMapper.toItemWithBookingsDto(item, comments);
    }

    @Override
    public List<ItemWithBookingsDto> getAllUserItems(Long userId) {
        log.info("Получение всех вещей пользователя {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        Map<Long, List<Booking>> bookingsByItem = bookingRepository.findAllByItemIdsOrderByItemAndStart(itemIds).stream()
                .filter(b -> b.getStatus() == Status.APPROVED)
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<CommentDto>> commentsByItem = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        c -> c.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), Collections.emptyList());

                    BookingShortDto lastBooking = null;
                    BookingShortDto nextBooking = null;

                    if (!itemBookings.isEmpty()) {
                        lastBooking = itemBookings.stream()
                                .filter(b -> b.getStart().isBefore(now))
                                .reduce((first, second) -> second)
                                .map(this::mapToBookingShortDto)
                                .orElse(null);

                        nextBooking = itemBookings.stream()
                                .filter(b -> b.getStart().isAfter(now))
                                .findFirst()
                                .map(this::mapToBookingShortDto)
                                .orElse(null);
                    }

                    return ItemMapper.toItemWithBookingsDto(
                            item,
                            lastBooking,
                            nextBooking,
                            commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        log.info("Добавление комментария к вещи {} пользователем {}", itemId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> completedBookings = bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        userId, itemId, now, Status.APPROVED);

        if (completedBookings.isEmpty()) {
            throw new ValidationException("Пользователь может оставить отзыв только после завершения аренды вещи");
        }

        if (commentRequestDto.getText() == null || commentRequestDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    private void validateItemData(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new IllegalArgumentException("Статус доступности должен быть указан");
        }
    }

    private BookingShortDto mapToBookingShortDto(Booking booking) {
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }
}