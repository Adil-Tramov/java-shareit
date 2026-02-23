package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemShortDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}