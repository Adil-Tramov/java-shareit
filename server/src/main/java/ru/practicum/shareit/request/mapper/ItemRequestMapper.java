package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemDataForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.dto.RequestDtoResponseWithMD;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", ignore = true)
    @Mapping(target = "created", ignore = true)
    @Mapping(target = "items", ignore = true)
    ItemRequest mapToItemRequest(ItemRequestDto itemRequestDto);

    ItemRequestDtoResponse mapToItemRequestDtoResponse(ItemRequest itemRequest);

    @Mapping(source = "request.id", target = "requestId")
    ItemDataForRequestDto mapToItemDataForRequestDto(Item item);

    default RequestDtoResponseWithMD mapToRequestDtoResponseWithMD(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }

        RequestDtoResponseWithMD response = new RequestDtoResponseWithMD();
        response.setId(itemRequest.getId());
        response.setDescription(itemRequest.getDescription());
        response.setCreated(itemRequest.getCreated());

        if (itemRequest.getItems() != null && !itemRequest.getItems().isEmpty()) {
            List<ItemDataForRequestDto> items = itemRequest.getItems().stream()
                    .map(this::mapToItemDataForRequestDto)
                    .collect(Collectors.toList());
            response.setItems(items);
        } else {
            response.setItems(Collections.emptyList()); // Пустой список, если нет items
        }

        return response;
    }

    List<RequestDtoResponseWithMD> mapToRequestDtoResponseWithMD(List<ItemRequest> itemRequests);
}