package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class ItemRequestListDto {
    @JsonValue
    private List<RequestDtoResponseWithMD> requests;
}