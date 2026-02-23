package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class UserDtoUpdate {
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Некорректное имя")
    @Size(max = 255)
    private String name;

    @Email(message = "Некорректный email")
    @Size(max = 255)
    private String email;
}