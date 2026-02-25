package ru.yandex.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;
import ru.yandex.practicum.shareit.request.model.ItemRequest;

@Mapper(componentModel = "spring")
public interface RequestMapper {
    RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

    @Mapping(target = "requestorId", source = "requestor.id")
    @Mapping(target = "items", ignore = true)
    ItemRequestDto toItemRequestDto(ItemRequest request);

    @Mapping(target = "requestor", ignore = true)
    ItemRequest toItemRequest(ItemRequestDto requestDto);
}