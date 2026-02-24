package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto createRequest(Long userId, ItemRequestCreateDto createDto);
    List<ItemRequestDto> getUserRequests(Long userId);
    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);
    ItemRequestDto getRequestById(Long userId, Long requestId);
}