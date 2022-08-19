package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {
    private final RequestService requestService;

    @PostMapping
    public ItemRequestDto addItemRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody ItemRequestCreateDto itemRequestCreateDto
    ) {
        return requestService.addItemRequest(itemRequestCreateDto, userId);
    }

    @GetMapping
    public List<ItemRequestWithResponsesDto> getAllItemRequestsWithResponsesCurrentUser(
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        return requestService.getAllItemRequestsWithResponsesCurrentUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestWithResponsesDto> getAllItemRequests(
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "1") Integer size,
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return requestService.getAllItemRequests(from, size, userId);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithResponsesDto getItemRequestWithResponsesById(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable long requestId
    ) {
        return requestService.getItemRequestWithResponsesById(requestId, userId);
    }
}
