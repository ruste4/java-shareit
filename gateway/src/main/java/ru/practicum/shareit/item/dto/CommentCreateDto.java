package ru.practicum.shareit.item.dto;

import lombok.ToString;

import javax.validation.constraints.NotBlank;

@ToString
public class CommentCreateDto {
    @NotBlank
    private String text;
}
