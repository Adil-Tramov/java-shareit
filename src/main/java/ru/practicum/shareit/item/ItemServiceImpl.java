package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        User user = getUserOrThrow(userId);
        validateItemData(itemDto);
        Item item = ItemMapper.toItem(itemDto, user);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Пользователь с ID " + userId + " не является владельцем вещи");
        }

        validateUpdateData(itemDto);

        updateItemFields(existingItem, itemDto);

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        List<CommentDto> commentDtos = getCommentsForItem(itemId);

        System.out.println("Item " + itemId + " has " + commentDtos.size() + " comments");

        if (isUserOwner(userId, item)) {
            return getItemWithBookings(item, now, commentDtos);
        } else {
            return ItemMapper.toItemWithBookingsDto(item, commentDtos);
        }
    }

    @Override
    public List<ItemWithBookingsDto> getAllUserItems(Long userId) {
        getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        LocalDateTime now = LocalDateTime.now();

        Map<Long, List<CommentDto>> commentsByItemId = getCommentsForItems(items);

        Map<Long, BookingInfo> bookingInfoByItemId = getBookingInfoForItemsOptimized(items, now);

        return items.stream()
                .map(item -> {
                    BookingInfo info = bookingInfoByItemId.getOrDefault(item.getId(),
                            new BookingInfo(null, null));

                    return ItemMapper.toItemWithBookingsDto(
                            item,
                            info.getLastBooking(),
                            info.getNextBooking(),
                            commentsByItemId.getOrDefault(item.getId(), Collections.emptyList())
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return List.of();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        checkUserHasCompletedBooking(userId, itemId, now);

        Comment comment = createComment(commentRequestDto, item, author, now);
        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Вещь с ID " + itemId + " не найдена"));
    }

    private void validateItemData(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Статус доступности должен быть указан");
        }
    }

    private void validateUpdateData(ItemDto itemDto) {
        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        }
        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание не может быть пустым");
        }
    }

    private void updateItemFields(Item item, ItemDto itemDto) {
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
    }

    private boolean isUserOwner(Long userId, Item item) {
        return userId != null && item.getOwner().getId().equals(userId);
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        return commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private ItemWithBookingsDto getItemWithBookings(Item item, LocalDateTime now, List<CommentDto> comments) {
        List<Booking> lastBookings = bookingRepository.findLastBooking(item.getId(), now);
        List<Booking> nextBookings = bookingRepository.findNextBooking(item.getId(), now);

        BookingShortDto lastBookingDto = createBookingShortDto(lastBookings);
        BookingShortDto nextBookingDto = createBookingShortDto(nextBookings);

        return ItemMapper.toItemWithBookingsDto(item, lastBookingDto, nextBookingDto, comments);
    }

    private BookingShortDto createBookingShortDto(List<Booking> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }
        Booking booking = bookings.get(0);
        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker().getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }

    private Map<Long, List<CommentDto>> getCommentsForItems(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));
    }

    private Map<Long, BookingInfo> getBookingInfoForItemsOptimized(List<Item> items, LocalDateTime now) {
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Booking> allBookings = bookingRepository.findAllByItemIdsOrderByItemAndStart(itemIds);

        Map<Long, List<Booking>> bookingsByItemId = allBookings.stream()
                .filter(booking -> booking.getStatus() == Status.APPROVED)
                .collect(Collectors.groupingBy(
                        booking -> booking.getItem().getId()
                ));

        return items.stream()
                .collect(Collectors.toMap(
                        Item::getId,
                        item -> {
                            List<Booking> itemBookings = bookingsByItemId.getOrDefault(item.getId(), Collections.emptyList());

                            // Последнее бронирование (с датой начала в прошлом)
                            Booking lastBooking = itemBookings.stream()
                                    .filter(b -> b.getStart().isBefore(now))
                                    .reduce((first, second) -> second) // Берем последнее (с наибольшей датой)
                                    .orElse(null);

                            // Следующее бронирование (с датой начала в будущем)
                            Booking nextBooking = itemBookings.stream()
                                    .filter(b -> b.getStart().isAfter(now))
                                    .findFirst()
                                    .orElse(null);

                            return new BookingInfo(
                                    createBookingShortDto(lastBooking != null ? List.of(lastBooking) : Collections.emptyList()),
                                    createBookingShortDto(nextBooking != null ? List.of(nextBooking) : Collections.emptyList())
                            );
                        }
                ));
    }

    private void checkUserHasCompletedBooking(Long userId, Long itemId, LocalDateTime now) {
        List<Booking> completedBookings = bookingRepository
                .findByBookerIdAndItemIdAndEndBeforeAndStatusOrderByEndDesc(
                        userId, itemId, now, Status.APPROVED);

        if (completedBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Пользователь может оставить отзыв только после завершения аренды вещи");
        }
    }

    private Comment createComment(CommentRequestDto commentRequestDto, Item item, User author, LocalDateTime now) {
        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);
        return comment;
    }

    private static class BookingInfo {
        private final BookingShortDto lastBooking;
        private final BookingShortDto nextBooking;

        public BookingInfo(BookingShortDto lastBooking, BookingShortDto nextBooking) {
            this.lastBooking = lastBooking;
            this.nextBooking = nextBooking;
        }

        public BookingShortDto getLastBooking() {
            return lastBooking;
        }

        public BookingShortDto getNextBooking() {
            return nextBooking;
        }
    }
}