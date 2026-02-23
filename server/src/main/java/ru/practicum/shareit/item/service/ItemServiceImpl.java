package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoUpdate;
import ru.practicum.shareit.item.dto.ItemListDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemServiceImpl implements ItemService {
    private final ItemRepository items;
    private final UserRepository users;
    private final BookingRepository bookings;
    private final CommentRepository comments;
    private final ItemMapper mapper;
    private final ItemRequestRepository itemRequests;

    @Override
    @Transactional
    public ItemDtoResponse createItem(ItemDto item, Long userId) {
        User owner = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Пользователя с id=%s нет", userId)));

        Item newItem = mapper.mapToItemFromItemDto(item);
        newItem.setOwner(owner);

        if (item.getRequestId() != null) {
            ItemRequest itemRequest = itemRequests.findById(item.getRequestId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            String.format("Запроса с id=%s нет", item.getRequestId())));
            newItem.setRequest(itemRequest);
        }

        return mapper.mapToItemDtoResponse(items.save(newItem));
    }

    @Override
    @Transactional
    public ItemDtoResponse updateItem(Long itemId, Long userId, ItemDtoUpdate item) {
        Item updateItem = items.findById(itemId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Предмета с id=%s нет", itemId)));
        if (!updateItem.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Предмет с id=%s пользователю с id=%s не пренадлежит", itemId, userId));
        }
        return mapper.mapToItemDtoResponse(items.save(mapper.mapToItemFromItemDtoUpdate(item, updateItem)));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDtoResponse getItemByItemId(Long userId, Long itemId) {
        Item item = items.findById(itemId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Предмета с id=%s нет", itemId)));
        ItemDtoResponse itemDtoResponse = mapper.mapToItemDtoResponse(item);
        if (item.getOwner().getId().equals(userId)) {
            itemDtoResponse.setLastBooking(mapper
                    .mapToBookingShortDto(bookings
                            .findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(
                                    itemId, LocalDateTime.now(), Status.APPROVED).orElse(null)
                    ));
            itemDtoResponse.setNextBooking(mapper.mapToBookingShortDto(bookings
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            itemId, LocalDateTime.now(), Status.APPROVED).orElse(null)
            ));
            return itemDtoResponse;
        }
        return itemDtoResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ItemListDto getPersonalItems(Pageable pageable, Long userId) {
        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s не существует", userId));
        }
        List<ItemDtoResponse> personalItems = items.findAllByOwnerIdOrderByIdAsc(pageable, userId).stream()
                .map(mapper::mapToItemDtoResponse).collect(Collectors.toList());
        for (ItemDtoResponse item : personalItems) {
            item.setLastBooking(mapper.mapToBookingShortDto(bookings.findFirstByItemIdAndStartBeforeAndStatusOrderByEndDesc(
                    item.getId(), LocalDateTime.now(), Status.APPROVED).orElse(null)));
            item.setNextBooking(mapper.mapToBookingShortDto(bookings
                    .findFirstByItemIdAndStartAfterAndStatusOrderByStartAsc(
                            item.getId(), LocalDateTime.now(), Status.APPROVED).orElse(null)
            ));
        }
        return ItemListDto.builder().items(personalItems).build();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemListDto getFoundItems(Pageable pageable, String text) {
        if (text.isBlank()) {
            return ItemListDto.builder().items(new ArrayList<>()).build();
        }
        return ItemListDto.builder()
                .items(items.search(text, pageable).stream()
                        .map(mapper::mapToItemDtoResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(Long itemId, Long userId, CommentDto commentDto) {
        if (!bookings.existsBookingByItemIdAndBookerIdAndStatusAndEndIsBefore(itemId, userId,
                Status.APPROVED, LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("У пользователя с id=%s не было ни одной брони на предмет с id=%s", userId, itemId));
        } else {
            User author = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s нет", userId)));
            Item item = items.findById(itemId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Предмета с id=%s нет", itemId)));
            Comment comment = mapper.mapToCommentFromCommentDto(commentDto);
            comment.setItem(item);
            comment.setAuthor(author);
            comment.setCreated(LocalDateTime.now());
            return mapper.mapToCommentDtoResponseFromComment(comments.save(comment));
        }
    }
}