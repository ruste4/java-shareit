package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestMapper;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.requests.exceptions.ItemRequestNotFound;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserService userService;

    @Override
    public ItemRequestDto addItemRequest(ItemRequestCreateDto itemRequestCreateDto, long userId) {
        ItemRequest itemRequest = new ItemRequest();
        String description = itemRequestCreateDto.getDescription();
        User requester = userService.getUserById(userId);
        LocalDateTime createdDateTime = LocalDateTime.now();

        itemRequest.setDescription(description);
        itemRequest.setRequester(requester);
        itemRequest.setCreated(createdDateTime);

        itemRequestRepository.save(itemRequest);

        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequest> getAllItemRequestsByRequester(User requester) {
        return itemRequestRepository.findAllByRequesterOrderByCreatedDesc(requester);
    }

    @Override
    public List<ItemRequestWithResponsesDto> getAllItemRequestsWithResponsesCurrentUser(long currentUserId) {
        User currentUser = userService.getUserById(currentUserId);

        List<ItemRequest> itemRequests = getAllItemRequestsByRequester(currentUser);
        return itemRequests.stream()
                .map(this::addResponsesForItemRequest)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestWithResponsesDto getItemRequestWithResponsesById(long id, long userId) {
        ItemRequest itemRequest = getItemRequestById(id);
        userService.getUserById(userId);
        return addResponsesForItemRequest(itemRequest);
    }

    private ItemRequestWithResponsesDto addResponsesForItemRequest(ItemRequest itemRequest) {
        List<Item> responses = itemRequestRepository.getAllItemByRequestId(itemRequest);

        ItemRequestWithResponsesDto itemRequestWithResponses = ItemRequestMapper
                .toItemRequestWithResponsesDto(itemRequest);

        List<ItemRequestWithResponsesDto.Item> itemsForItemRequestWithResponsesDto = responses.stream()
                .map(ItemRequestWithResponsesDto.Item::new)
                .collect(Collectors.toList());

        itemRequestWithResponses.setItems(itemsForItemRequestWithResponsesDto);

        return itemRequestWithResponses;
    }

    @Override
    public ItemRequest getItemRequestById(long id) {
        return itemRequestRepository.findById(id).orElseThrow(
                () -> new ItemRequestNotFound(String.format("Item request with id:%s not found.", id))
        );
    }

    @Override
    public List<ItemRequestWithResponsesDto> getAllItemRequests(int from, int size, long userId) {
        User user = userService.getUserById(userId);
        PageRequest pageRequest = PageRequest.of(from, size, Sort.by("created").descending());

        return itemRequestRepository.findAllByRequesterNotOrderByCreatedDesc(user, pageRequest)
                .map(this::addResponsesForItemRequest).toList();

    }

}
