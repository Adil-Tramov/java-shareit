package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserListDto {
    private List<UserDtoResponse> users;
}