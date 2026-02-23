package ru.practicum.shareit.request.service;

import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.dto.ItemRequestListDto;
import ru.practicum.shareit.request.dto.RequestDtoResponseWithMD;

public interface ItemRequestService {
    ItemRequestDtoResponse createItemRequest(ItemRequestDto itemRequestDto, Long requesterId);

    ItemRequestListDto getPrivateRequests(PageRequest pageRequest, Long requesterId);

    ItemRequestListDto getOtherRequests(PageRequest pageRequest, Long requesterId);

    RequestDtoResponseWithMD getItemRequest(Long userId, Long requestId);
}