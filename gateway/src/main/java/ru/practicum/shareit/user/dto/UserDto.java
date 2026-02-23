package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@Builder
public class UserDto {
    @NotBlank(message = "Имя не может быть пустым")
    @Size(max = 255, message = "Имя не может быть длиннее 255 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(max = 512, message = "Email не может быть длиннее 512 символов")
    private String email;
}