package ru.practicum.shareit.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-user")
public class TestUserController {
    @GetMapping
    public String test() {
        return "User controller works!";
    }
}