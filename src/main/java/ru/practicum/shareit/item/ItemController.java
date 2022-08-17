package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
    public List<ItemWithBookingDatesDto> getAll(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "100") @Min(1) Integer size
    ) {
        return itemService.getAllByOwnerId(userId, from, size);
    }

    @GetMapping("/search")
    public List<ItemDto> searchByNameAndDescription(
            @RequestParam() String text,
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "100") @Min(1) Integer size
    ) {
        return itemService.searchByNameOrDescription(text, from, size).stream()
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
