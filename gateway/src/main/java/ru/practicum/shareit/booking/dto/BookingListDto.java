package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class BookingListDto {
    @JsonValue
    private List<BookingDtoResponse> bookings;
}