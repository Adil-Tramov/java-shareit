package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
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
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден"));
    }

    private ItemRequest getRequestOrThrow(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Запрос с ID " + requestId + " не найден"));
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto createDto) {
        User requestor = getUserOrThrow(userId);

        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(createDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        getUserOrThrow(userId);

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorId(userId, SORT_BY_CREATED_DESC);

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequestId;

        if (requestIds.isEmpty()) {
            itemsByRequestId = Collections.emptyMap();
        } else {
            List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

            itemsByRequestId = items.stream()
                    .filter(item -> item.getRequestId() != null)
                    .collect(Collectors.groupingBy(
                            Item::getRequestId,
                            Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                    ));
        }

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDtoWithItems(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);

        if (from < 0 || size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Параметры пагинации должны быть положительными: from=" + from + ", size=" + size);
        }

        List<ItemRequest> requests = itemRequestRepository.findAllByRequestorIdNot(userId, SORT_BY_CREATED_DESC);

        int start = from;
        int end = Math.min(from + size, requests.size());

        if (start >= requests.size()) {
            return Collections.emptyList();
        }

        List<ItemRequest> pagedRequests = requests.subList(start, end);

        List<Long> requestIds = pagedRequests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequestId;

        if (requestIds.isEmpty()) {
            itemsByRequestId = Collections.emptyMap();
        } else {
            List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

            itemsByRequestId = items.stream()
                    .filter(item -> item.getRequestId() != null)
                    .collect(Collectors.groupingBy(
                            Item::getRequestId,
                            Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                    ));
        }

        return pagedRequests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDtoWithItems(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);
        ItemRequest request = getRequestOrThrow(requestId);

        List<Item> items = itemRepository.findAllByRequestIdIn(List.of(requestId));

        List<ItemDto> itemDtos = items.stream()
                .filter(item -> requestId.equals(item.getRequestId()))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestDtoWithItems(request, itemDtos);
    }
}