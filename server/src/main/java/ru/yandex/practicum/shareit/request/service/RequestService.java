package ru.yandex.practicum.shareit.request.service;

import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface RequestService {
    ItemRequestDto getRequestById(Long userId, Long requestId);

    List<ItemRequestDto> getUserRequests(Long userId);

    List<ItemRequestDto> getAllRequests(Long userId, int from, int size);

    ItemRequestDto createRequest(Long userId, String description);
}