package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ItemListDto {
    @JsonValue
    private List<ItemDtoResponse> items;
}