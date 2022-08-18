package ru.practicum.shareit.request.dto;

import lombok.ToString;

import javax.validation.constraints.NotBlank;

@ToString
public class RequestCreateDto {
    @NotBlank
    private String description;
}
