package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.RequestCreateDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestBody @Valid RequestCreateDto requestCreateDto
    ) {
        log.info("Add request with userId={}, {}", userId, requestCreateDto);
        return requestClient.addRequest(requestCreateDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequestsCurrentUser(
            @RequestHeader("X-Sharer-User-Id") long userId
    ) {
        log.info("Get all requests current user with id={}", userId);
        return requestClient.getAllItemRequestsCurrentUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
            @Positive @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Get all requests with from={}, size={}", from, size);
        return requestClient.getAllItemRequests(from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestWithResponsesById(@PathVariable long requestId) {
        log.info("Get request with id={}", requestId);
        return requestClient.getRequestById(requestId);
    }
}
