package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
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
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    private void validateItemData(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }
    }

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
        Item existingItem = getItemOrThrow(itemId);

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        if (itemDto.getRequestId() != null) {
            existingItem.setRequestId(itemDto.getRequestId());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = getItemOrThrow(itemId);
        ItemDto itemDto = ItemMapper.toItemDto(item);

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        itemDto.setComments(comments);

        return itemDto;
    }

    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        getUserOrThrow(userId);

        List<Item> items = itemRepository.findAllByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<CommentDto>> commentsByItemId = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toCommentDto, Collectors.toList())
                ));

        return items.stream()
                .map(item -> {
                    BookingShortDto lastBooking = findLastBooking(item.getId(), now);
                    BookingShortDto nextBooking = findNextBooking(item.getId(), now);
                    List<CommentDto> itemComments = commentsByItemId.getOrDefault(item.getId(), Collections.emptyList());

                    return ItemMapper.toItemDtoWithBookings(item, lastBooking, nextBooking, itemComments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableByText(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentCreateDto commentDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        boolean hasBookedAndFinished = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, LocalDateTime.now(), BookingStatus.APPROVED);

        if (!hasBookedAndFinished) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда ещё не завершена");
        }

        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    private BookingShortDto findLastBooking(Long itemId, LocalDateTime now) {
        List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now,
                Sort.by(Sort.Direction.DESC, "start"));

        if (lastBookings.isEmpty()) {
            return null;
        }

        Booking lastBooking = lastBookings.get(0);
        return BookingShortDto.builder()
                .id(lastBooking.getId())
                .bookerId(lastBooking.getBooker().getId())
                .build();
    }

    private BookingShortDto findNextBooking(Long itemId, LocalDateTime now) {
        List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now,
                Sort.by(Sort.Direction.ASC, "start"));

        if (nextBookings.isEmpty()) {
            return null;
        }

        Booking nextBooking = nextBookings.get(0);
        return BookingShortDto.builder()
                .id(nextBooking.getId())
                .bookerId(nextBooking.getBooker().getId())
                .build();
    }
}