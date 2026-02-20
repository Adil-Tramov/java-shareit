package ru.practicum.shareit.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import ru.practicum.shareit.item.dto.ItemDto;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;

    @NotBlank(message = "Описание не может быть пустым")
    private String description;

    private LocalDateTime created;

    private List<ItemDto> items;
}