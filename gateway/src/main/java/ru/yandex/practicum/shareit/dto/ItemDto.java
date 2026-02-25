package ru.yandex.practicum.shareit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotBlank(message = "название не может быть пустым")
    private String name;

    @NotBlank(message = "описание не может быть пустым")
    private String description;

    @NotNull(message = "статус доступности должен быть указан")
    private Boolean available;

    private Long requestId;
}