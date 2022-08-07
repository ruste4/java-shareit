package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public ItemDto addItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @Valid @RequestBody ItemCreateDto itemCreateDto
    ) {
        return ItemMapper.toItemDto(itemService.addItem(userId, itemCreateDto));
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long itemId,
            @RequestBody ItemDto itemDto
    ) {
        itemDto.setId(itemId);

        return ItemMapper.toItemDto(itemService.updateItem(userId, itemDto));
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDatesDto getItem(
            @PathVariable long itemId,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {

        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemWithBookingDatesDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getAllByOwnerId(userId);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByNameAndDescription(@RequestParam() String text) {
        return itemService.searchByNameAndDescription(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(
            @Valid @RequestBody CommentCreateDto commentCreateDto,
            @PathVariable long itemId,
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        return CommentMapper.toCommentDto(itemService.addComment(commentCreateDto, itemId, userId));
    }
}
