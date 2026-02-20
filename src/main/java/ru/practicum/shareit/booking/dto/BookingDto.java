package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingItemDto item;
    private BookingUserDto booker;
    private BookingStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingItemDto {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BookingUserDto {
        private Long id;
        private String name;
    }
}