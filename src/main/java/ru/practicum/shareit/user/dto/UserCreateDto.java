package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@EqualsAndHashCode
public class UserCreateDto {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

}
