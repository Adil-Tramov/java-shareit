package ru.yandex.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.yandex.practicum.shareit.request.dto.ItemRequestDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        ItemRequestDto requestDto = ItemRequestDto.builder()
                .id(1L)
                .description("Need a drill")
                .requestorId(1L)
                .created(now)
                .items(List.of())
                .build();

        var result = json.write(requestDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.description");
        assertThat(result).hasJsonPathNumberValue("$.requestorId");
        assertThat(result).hasJsonPathStringValue("$.created");
        assertThat(result).hasJsonPathArrayValue("$.items");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":1,\"description\":\"Need a drill\",\"requestorId\":1}";

        ItemRequestDto requestDto = json.parse(content).getObject();

        assertThat(requestDto.getId()).isEqualTo(1);
        assertThat(requestDto.getDescription()).isEqualTo("Need a drill");
        assertThat(requestDto.getRequestorId()).isEqualTo(1);
    }
}