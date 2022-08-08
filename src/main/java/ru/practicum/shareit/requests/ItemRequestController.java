package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
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
            @RequestBody @Valid ItemRequestCreateDto itemRequestCreateDto
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
    public List<ItemRequestDto> getAllItemRequests(
            @RequestParam(defaultValue = "0") @Min(0) Integer from,
            @RequestParam(defaultValue = "1") @Min(1) Integer size
    ) {
        return requestService.getAllItemRequests(from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestWithResponsesDto getItemRequestWithResponsesById(@PathVariable long requestId) {
        return requestService.getItemRequestWithResponsesById(requestId);
    }
}
