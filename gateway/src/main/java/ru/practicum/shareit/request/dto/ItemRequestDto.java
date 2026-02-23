package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Builder
@Data
@Jacksonized
public class ItemRequestDto {
    @NotBlank(message = "поле description не должно быть пустым")
    @Size(max = 500, message = "Превышена максимальная длина описания")
    private String description;
}