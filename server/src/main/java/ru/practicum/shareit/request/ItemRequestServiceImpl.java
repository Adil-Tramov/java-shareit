package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
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
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestCreateDto createDto) {
        log.info("Создание запроса от пользователя {}", userId);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = new ItemRequest();
        request.setDescription(createDto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Запрос создан с ID {}", savedRequest.getId());

        return mapToDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя {}", userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<ItemRequest> requests = requestRepository.findAllByRequestorId(userId, SORT_BY_CREATED_DESC);
        return enrichRequestsWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов, пользователь {}, from={}, size={}", userId, from, size);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<ItemRequest> requests = requestRepository.findAllByRequestorIdNot(userId, SORT_BY_CREATED_DESC);

        int start = from;
        int end = Math.min(from + size, requests.size());

        if (start >= requests.size()) {
            return Collections.emptyList();
        }

        return enrichRequestsWithItems(requests.subList(start, end));
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса {} пользователем {}", requestId, userId);

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemRepository.findAllByRequestId(requestId);
        List<ItemDto> itemDtos = items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return mapToDto(request, itemDtos);
    }

    private List<ItemRequestDto> enrichRequestsWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        List<Item> items = itemRepository.findAllByRequestIdIn(requestIds);

        Map<Long, List<ItemDto>> itemsByRequestId = items.stream()
                .collect(Collectors.groupingBy(
                        Item::getRequestId,
                        Collectors.mapping(ItemMapper::toItemDto, Collectors.toList())
                ));

        return requests.stream()
                .map(request -> mapToDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    private ItemRequestDto mapToDto(ItemRequest request, List<ItemDto> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items)
                .build();
    }
}