package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.Status;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    @NotNull(message = "Поле start обязательно")
    @FutureOrPresent(message = "Начало бронирования не может быть в прошлом")
    private LocalDateTime start;

    @Future(message = "Конец бронирования не может быть в прошлом")
    @NotNull(message = "Поле end обязательно")
    private LocalDateTime end;

    @NotNull(message = "Поле itemId обязательно")
    @Min(value = 1, message = "Некорректный itemId")
    private Long itemId;

    private Status status;

    @AssertTrue(message = "Дата окончания должна быть позже даты начала")
    public boolean isEndAfterStart() {
        if (start == null || end == null) {
            return true;
        }
        return end.isAfter(start);
    }
}