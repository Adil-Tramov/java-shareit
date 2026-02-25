package ru.yandex.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.booking.model.Booking;
import ru.yandex.practicum.shareit.booking.repository.BookingRepository;
import ru.yandex.practicum.shareit.booking.mapper.BookingMapper;
import ru.yandex.practicum.shareit.exception.BadRequestException;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.CommentDto;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.mapper.ItemMapper;
import ru.yandex.practicum.shareit.item.model.Comment;
import ru.yandex.practicum.shareit.item.model.Item;
import ru.yandex.practicum.shareit.item.repository.CommentRepository;
import ru.yandex.practicum.shareit.item.repository.ItemRepository;
import ru.yandex.practicum.shareit.request.model.ItemRequest;
import ru.yandex.practicum.shareit.request.repository.RequestRepository;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

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
    private final RequestRepository requestRepository;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;

    @Override
    public List<ItemDto> getUserItems(Long userId) {
        log.info("Getting items for user: {}", userId);

        List<Item> items = itemRepository.findByOwnerIdOrderById(userId);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Comment>> commentsByItem = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        List<Booking> lastBookingsList = bookingRepository.findLastBookingsForOwner(userId, LocalDateTime.now());
        Map<Long, Booking> lastBookings = lastBookingsList.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b, (b1, b2) -> b1));

        List<Booking> nextBookingsList = bookingRepository.findNextBookingsForOwner(userId, LocalDateTime.now());
        Map<Long, Booking> nextBookings = nextBookingsList.stream()
                .collect(Collectors.toMap(b -> b.getItem().getId(), b -> b, (b1, b2) -> b1));

        return items.stream()
                .map(item -> {
                    ItemDto itemDto = itemMapper.toItemDto(item);

                    if (lastBookings.containsKey(item.getId())) {
                        itemDto.setLastBooking(bookingMapper.toBookingShortDto(lastBookings.get(item.getId())));
                    }

                    if (nextBookings.containsKey(item.getId())) {
                        itemDto.setNextBooking(bookingMapper.toBookingShortDto(nextBookings.get(item.getId())));
                    }

                    if (commentsByItem.containsKey(item.getId())) {
                        itemDto.setComments(commentsByItem.get(item.getId()).stream()
                                .map(itemMapper::toCommentDto)
                                .collect(Collectors.toList()));
                    } else {
                        itemDto.setComments(Collections.emptyList());
                    }

                    return itemDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long userId, Long itemId) {
        log.info("Getting item {} for user: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        ItemDto itemDto = itemMapper.toItemDto(item);

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            bookingRepository.findLastBookingForItem(itemId, now)
                    .ifPresent(booking -> itemDto.setLastBooking(bookingMapper.toBookingShortDto(booking)));
            bookingRepository.findNextBookingForItem(itemId, now)
                    .ifPresent(booking -> itemDto.setNextBooking(bookingMapper.toBookingShortDto(booking)));
        }

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        log.debug("Found {} comments for item {}", comments.size(), itemId);

        if (!comments.isEmpty()) {
            itemDto.setComments(comments.stream()
                    .map(itemMapper::toCommentDto)
                    .collect(Collectors.toList()));
        } else {
            itemDto.setComments(Collections.emptyList());
        }

        return itemDto;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Searching items with text: {}", text);
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableItems(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Creating item {} for user: {}", itemDto, userId);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с id " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        item = itemRepository.save(item);
        ItemDto savedItemDto = itemMapper.toItemDto(item);
        savedItemDto.setComments(Collections.emptyList());

        return savedItemDto;
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Updating item {} with data {} for user: {}", itemId, itemDto, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        item = itemRepository.save(item);
        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, String text) {
        log.info("Adding comment to item {} from user: {}", itemId, userId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id " + itemId + " не найдена"));

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(userId, itemId, now);

        log.debug("Found {} completed bookings for user {} on item {}", bookings.size(), userId, itemId);

        if (bookings.isEmpty()) {
            throw new BadRequestException("Пользователь не брал эту вещь в аренду или аренда ещё не завершена");
        }

        Comment comment = Comment.builder()
                .text(text)
                .item(item)
                .author(author)
                .created(now)
                .build();

        comment = commentRepository.save(comment);
        log.info("Comment saved with id: {} for item: {}", comment.getId(), itemId);

        return itemMapper.toCommentDto(comment);
    }
}
