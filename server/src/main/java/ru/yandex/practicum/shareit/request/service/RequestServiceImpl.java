package ru.yandex.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.shareit.exception.NotFoundException;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.item.mapper.ItemMapper;
import ru.yandex.practicum.shareit.item.repository.ItemRepository;
import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;
import ru.yandex.practicum.shareit.request.mapper.RequestMapper;
import ru.yandex.practicum.shareit.request.model.ItemRequest;
import ru.yandex.practicum.shareit.request.repository.RequestRepository;
import ru.yandex.practicum.shareit.user.model.User;
import ru.yandex.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Getting request {} for user: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id " + requestId + " не найден"));

        ItemRequestDto requestDto = requestMapper.toItemRequestDto(request);

        List<ItemDto> items = itemRepository.findAllByRequestId(requestId).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());

        requestDto.setItems(items);

        return requestDto;
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Getting requests for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        List<ItemRequest> requests = requestRepository.findByRequestorIdOrderByCreatedDesc(userId);
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = requestMapper.toItemRequestDto(request);
                    dto.setItems(itemsByRequest.getOrDefault(request.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, int from, int size) {
        log.info("Getting all requests for user: {} from {} size {}", userId, from, size);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        List<ItemRequest> requests = requestRepository.findAllExceptUser(userId, pageable);

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<ItemDto>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.groupingBy(ItemDto::getRequestId));

        return requests.stream()
                .map(request -> {
                    ItemRequestDto dto = requestMapper.toItemRequestDto(request);
                    dto.setItems(itemsByRequest.getOrDefault(request.getId(), List.of()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, String description) {
        log.info("Creating request for user: {} with description: {}", userId, description);

        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));

        ItemRequest request = ItemRequest.builder()
                .description(description)
                .requestor(requestor)
                .created(LocalDateTime.now())
                .build();

        request = requestRepository.save(request);
        ItemRequestDto requestDto = requestMapper.toItemRequestDto(request);
        requestDto.setItems(List.of());

        return requestDto;
    }
}