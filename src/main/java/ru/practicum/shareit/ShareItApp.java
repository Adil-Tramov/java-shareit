package ru.practicum.shareit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ru.practicum.shareit")
@EntityScan(basePackages = "ru.practicum.shareit")
@EnableJpaRepositories(basePackages = "ru.practicum.shareit")
public class ShareItApp {
    public static void main(String[] args) {
        SpringApplication.run(ShareItApp.class, args);
    }
}