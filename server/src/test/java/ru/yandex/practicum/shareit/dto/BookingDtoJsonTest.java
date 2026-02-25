package ru.yandex.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.yandex.practicum.shareit.booking.dto.BookingDto;
import ru.yandex.practicum.shareit.booking.model.BookingStatus;
import ru.yandex.practicum.shareit.item.dto.ItemDto;
import ru.yandex.practicum.shareit.user.dto.UserDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void testSerialize() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        ItemDto itemDto = ItemDto.builder().id(1L).name("Drill").build();
        UserDto userDto = UserDto.builder().id(2L).name("User").build();

        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(now)
                .end(now.plusDays(1))
                .item(itemDto)
                .booker(userDto)
                .status(BookingStatus.WAITING)
                .build();

        var result = json.write(bookingDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.start");
        assertThat(result).hasJsonPathStringValue("$.end");
        assertThat(result).hasJsonPathNumberValue("$.item.id");
        assertThat(result).hasJsonPathNumberValue("$.booker.id");
        assertThat(result).hasJsonPathStringValue("$.status");
    }
}