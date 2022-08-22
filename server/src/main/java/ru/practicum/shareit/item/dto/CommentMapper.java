package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Comment;

public class CommentMapper {

    public static Comment toComment(CommentCreateDto commentCreateDto) {
        return Comment.builder().text(commentCreateDto.getText()).build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }

}
