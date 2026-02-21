package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, ItemRequestDto requestDto) {
        User requestor = getUserOrThrow(userId);

        ItemRequest request = ItemRequestMapper.toItemRequest(requestDto, requestor);
        request.setCreated(LocalDateTime.now());

        ItemRequest savedRequest = requestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        getUserOrThrow(userId);

        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        getUserOrThrow(userId);

        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        getUserOrThrow(userId);

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с ID " + requestId + " не найден"));

        return ItemRequestMapper.toItemRequestDto(request);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }
}