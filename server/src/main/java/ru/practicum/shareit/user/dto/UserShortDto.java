package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortDto {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;
}