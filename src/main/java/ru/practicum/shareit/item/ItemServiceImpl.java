package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Sort;
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

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User user = getUserOrThrow(userId);
        validateItemData(itemDto);
        Item item = ItemMapper.toItem(itemDto, user);
        item.setRequestId(itemDto.getRequestId());
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Пользователь с ID " + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null && itemDto.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        }
        if (itemDto.getDescription() != null && itemDto.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Описание не может быть пустым");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long userId, Long itemId) {
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        List<Comment> comments = commentRepository.findAllByItemId(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> CommentDto.builder()
                        .id(comment.getId())
                        .text(comment.getText())
                        .authorName(comment.getAuthor().getName())
                        .created(comment.getCreated())
                        .build())
                .collect(Collectors.toList());

        System.out.println("Item " + itemId + " has " + commentDtos.size() + " comments");

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
            List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);

            BookingShortDto lastBookingDto = lastBookings.isEmpty() ? null :
                    BookingShortDto.builder()
                            .id(lastBookings.get(0).getId())
                            .bookerId(lastBookings.get(0).getBooker().getId())
                            .start(lastBookings.get(0).getStart())
                            .end(lastBookings.get(0).getEnd())
                            .status(lastBookings.get(0).getStatus())
                            .build();

            BookingShortDto nextBookingDto = nextBookings.isEmpty() ? null :
                    BookingShortDto.builder()
                            .id(nextBookings.get(0).getId())
                            .bookerId(nextBookings.get(0).getBooker().getId())
                            .start(nextBookings.get(0).getStart())
                            .end(nextBookings.get(0).getEnd())
                            .status(nextBookings.get(0).getStatus())
                            .build();

            return ItemWithBookingsDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .ownerId(item.getOwner().getId())
                    .requestId(item.getRequestId())
                    .lastBooking(lastBookingDto)
                    .nextBooking(nextBookingDto)
                    .comments(commentDtos)
                    .build();
        } else {
            return ItemWithBookingsDto.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .available(item.getAvailable())
                    .ownerId(item.getOwner().getId())
                    .requestId(item.getRequestId())
                    .comments(commentDtos)
                    .build();
        }
    }

    @Override
    public List<ItemWithBookingsDto> getAllUserItems(Long userId) {
        getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwnerIdOrderByIdAsc(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<CommentDto>> commentsByItemId;
        if (itemIds.isEmpty()) {
            commentsByItemId = Collections.emptyMap();
        } else {
            commentsByItemId = commentRepository.findAllByItemIdIn(itemIds)
                    .stream()
                    .collect(Collectors.groupingBy(
                            comment -> comment.getItem().getId(),
                            Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                    ));
        }

        return items.stream()
                .map(item -> {
                    List<Booking> lastBookings = bookingRepository.findLastBooking(item.getId(), now);
                    List<Booking> nextBookings = bookingRepository.findNextBooking(item.getId(), now);

                    BookingShortDto lastBookingDto = lastBookings.isEmpty() ? null :
                            BookingShortDto.builder()
                                    .id(lastBookings.get(0).getId())
                                    .bookerId(lastBookings.get(0).getBooker().getId())
                                    .start(lastBookings.get(0).getStart())
                                    .end(lastBookings.get(0).getEnd())
                                    .status(lastBookings.get(0).getStatus())
                                    .build();

                    BookingShortDto nextBookingDto = nextBookings.isEmpty() ? null :
                            BookingShortDto.builder()
                                    .id(nextBookings.get(0).getId())
                                    .bookerId(nextBookings.get(0).getBooker().getId())
                                    .start(nextBookings.get(0).getStart())
                                    .end(nextBookings.get(0).getEnd())
                                    .status(nextBookings.get(0).getStatus())
                                    .build();

                    return ItemWithBookingsDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .ownerId(item.getOwner().getId())
                            .requestId(item.getRequestId())
                            .lastBooking(lastBookingDto)
                            .nextBooking(nextBookingDto)
                            .comments(commentsByItemId.getOrDefault(item.getId(), Collections.emptyList()))
                            .build();
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
        System.out.println("\n========== ADD COMMENT DEBUG START ==========");
        System.out.println("User ID: " + userId);
        System.out.println("Item ID: " + itemId);
        System.out.println("Comment text: " + commentRequestDto.getText());

        User author = getUserOrThrow(userId);
        System.out.println("Author found: " + author.getName() + " (ID: " + author.getId() + ")");

        Item item = getItemOrThrow(itemId);
        System.out.println("Item found: " + item.getName() + " (ID: " + item.getId() + ")");
        System.out.println("Item owner ID: " + item.getOwner().getId());

        LocalDateTime now = LocalDateTime.now();
        System.out.println("Current time: " + now);

        System.out.println("\nSearching for bookings with:");
        System.out.println("- bookerId: " + userId);
        System.out.println("- itemId: " + itemId);
        System.out.println("- status: " + Status.APPROVED);

        List<Booking> userBookings = bookingRepository
                .findByBookerIdAndItemIdAndStatus(userId, itemId, Status.APPROVED);

        System.out.println("\nFound " + userBookings.size() + " APPROVED bookings:");

        if (userBookings.isEmpty()) {
            System.out.println("NO APPROVED BOOKINGS FOUND!");

            List<Booking> allUserBookings = bookingRepository.findByBookerId(userId, Sort.by(Sort.Direction.DESC, "start"));
            System.out.println("\nAll bookings for user " + userId + ": " + allUserBookings.size());
            for (Booking b : allUserBookings) {
                System.out.println("  Booking ID: " + b.getId() +
                        ", Item ID: " + b.getItem().getId() +
                        ", Status: " + b.getStatus() +
                        ", Start: " + b.getStart() +
                        ", End: " + b.getEnd());
            }
        } else {
            for (Booking booking : userBookings) {
                System.out.println("\n  Booking ID: " + booking.getId());
                System.out.println("    Start: " + booking.getStart());
                System.out.println("    End: " + booking.getEnd());
                System.out.println("    Status: " + booking.getStatus());
                System.out.println("    End before now? " + booking.getEnd().isBefore(now));
            }
        }

        boolean hasCompletedBooking = userBookings.stream()
                .anyMatch(booking -> booking.getEnd().isBefore(now));

        System.out.println("\nHas completed booking? " + hasCompletedBooking);

        if (!hasCompletedBooking) {
            System.out.println("ERROR: No completed bookings found! Throwing BAD_REQUEST");
            System.out.println("========== ADD COMMENT DEBUG END ==========\n");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User can only leave a comment after completing rental of the item");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        System.out.println("\nSaving comment...");
        Comment savedComment = commentRepository.save(comment);
        System.out.println("Comment saved with ID: " + savedComment.getId());

        CommentDto result = CommentDto.builder()
                .id(savedComment.getId())
                .text(savedComment.getText())
                .authorName(savedComment.getAuthor().getName())
                .created(savedComment.getCreated())
                .build();

        System.out.println("Returning CommentDto with id: " + result.getId());
        System.out.println("========== ADD COMMENT DEBUG END ==========\n");

        return result;
    }
}