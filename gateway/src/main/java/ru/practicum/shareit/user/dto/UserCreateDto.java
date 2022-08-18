package ru.practicum.shareit.user.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class UserCreateDto {
    @NotBlank
    private String name;
    @Email
    private String email;
}
