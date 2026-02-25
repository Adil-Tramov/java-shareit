package ru.yandex.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.yandex.practicum.shareit.item.dto.CommentDto;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws IOException {
        LocalDateTime now = LocalDateTime.now();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Great item")
                .authorName("John Doe")
                .created(now)
                .build();

        var result = json.write(commentDto);

        assertThat(result).hasJsonPathNumberValue("$.id");
        assertThat(result).hasJsonPathStringValue("$.text");
        assertThat(result).hasJsonPathStringValue("$.authorName");
        assertThat(result).hasJsonPathStringValue("$.created");
    }

    @Test
    void testDeserialize() throws IOException {
        String content = "{\"id\":1,\"text\":\"Great item\",\"authorName\":\"John Doe\"}";

        CommentDto commentDto = json.parse(content).getObject();

        assertThat(commentDto.getId()).isEqualTo(1);
        assertThat(commentDto.getText()).isEqualTo("Great item");
        assertThat(commentDto.getAuthorName()).isEqualTo("John Doe");
    }
}