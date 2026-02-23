package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class ItemDto {
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Некорректное имя")
    @Size(max = 255)
    @NotNull(message = "Поле name обязательно")
    private String name;

    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Некорректное описание")
    @Size(max = 500)
    @NotNull(message = "Поле description обязательно")
    private String description;

    @NotNull(message = "Поле available обязательно")
    private Boolean available;

    @Min(1)
    private Long requestId;
}