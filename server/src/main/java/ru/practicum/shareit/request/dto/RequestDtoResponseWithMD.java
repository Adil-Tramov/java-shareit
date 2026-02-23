package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequestDtoResponseWithMD {
    private Long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDataForRequestDto> items;
}