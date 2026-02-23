    package ru.practicum.shareit.request.service;

    import lombok.RequiredArgsConstructor;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.data.domain.PageRequest;
    import org.springframework.http.HttpStatus;
    import org.springframework.stereotype.Service;
    import org.springframework.web.server.ResponseStatusException;
    import ru.practicum.shareit.request.dto.ItemRequestDto;
    import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
    import ru.practicum.shareit.request.dto.ItemRequestListDto;
    import ru.practicum.shareit.request.dto.RequestDtoResponseWithMD;
    import ru.practicum.shareit.request.mapper.ItemRequestMapper;
    import ru.practicum.shareit.request.model.ItemRequest;
    import ru.practicum.shareit.request.repository.ItemRequestRepository;
    import ru.practicum.shareit.user.model.User;
    import ru.practicum.shareit.user.repository.UserRepository;

    import java.time.LocalDateTime;

    @Service
    @RequiredArgsConstructor(onConstructor = @__(@Autowired))
    public class ItemRequestServiceImpl implements ItemRequestService {
        private final ItemRequestRepository requests;
        private final UserRepository users;
        private final ItemRequestMapper mapper;

        @Override
        public ItemRequestDtoResponse createItemRequest(ItemRequestDto itemRequestDto, Long requesterId) {
            User user = users.findById(requesterId).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s нет", requesterId)));
            ItemRequest newRequest = mapper.mapToItemRequest(itemRequestDto);
            newRequest.setRequester(user);
            newRequest.setCreated(LocalDateTime.now());
            return mapper.mapToItemRequestDtoResponse(requests.save(newRequest));
        }

        @Override
        public ItemRequestListDto getPrivateRequests(PageRequest pageRequest, Long requesterId) {
            if (!users.existsById(requesterId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s нет", requesterId));
            }
            return ItemRequestListDto.builder()
                    .requests(mapper.mapToRequestDtoResponseWithMD(requests.findAllByRequesterId(pageRequest, requesterId)
                    )).build();
        }

        @Override
        public ItemRequestListDto getOtherRequests(PageRequest pageRequest, Long requesterId) {
            if (!users.existsById(requesterId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s нет", requesterId));
            }
            return ItemRequestListDto.builder()
                    .requests(mapper.mapToRequestDtoResponseWithMD(requests.findAllByRequesterIdNot(pageRequest, requesterId)
                    )).build();
        }

        @Override
        public RequestDtoResponseWithMD getItemRequest(Long userId, Long requestId) {
            if (!users.existsById(userId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Пользователя с id=%s нет", userId));
            }
            return mapper.mapToRequestDtoResponseWithMD(
                    requests.findById(requestId)
                            .orElseThrow(
                                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                            String.format("Запроса с id=%s нет", requestId)
                                    )
                            ));
        }
    }