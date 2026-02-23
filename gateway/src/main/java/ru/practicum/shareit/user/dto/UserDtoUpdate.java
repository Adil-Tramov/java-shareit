package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Getter;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Builder
@Getter
public class UserDtoUpdate {
    @Pattern(regexp = "^[^ ].*[^ ]$", message = "Некорректное имя")
    @Size(max = 255)
    private String name;

    @Email(message = "Некорректный email")
    private String email;
}