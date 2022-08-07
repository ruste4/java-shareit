package ru.practicum.shareit.requests;

import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;

import java.util.List;

public interface RequestService {
    /**
     * Добавить запрос
     */
    ItemRequestDto addItemRequest(ItemRequestCreateDto itemRequestCreateDto);

    /**
     * Получить все запросы текущего пользователя
     */
    List<ItemRequest> getAllItemRequestsByRequesterId(long currentUser);

    /**
     * Получить список запросов текущего пользователя вместе с ответами
     */
    List<ItemRequestWithResponsesDto> getAllItemRequestsWithResponsesCurrentUser(long currentUserId);

    /**
     * Получить запрос по id вместе с ответами
     */
    ItemRequestWithResponsesDto getItemRequestWithResponsesById(long id);

    /**
     * Получить запрос по id
     */
    ItemRequest getItemRequestById(long id);

    /**
     * Получить список запросов, созданные другими пользователями
     *
     * @param currentUserId - идентификатор текущего пользователя
     * @param from          - индекса первого элемента (для пагинации)
     * @param size          - количестов элементов отображения
     */
    List<ItemRequestDto> getItemRequestsListOtherUser(long currentUserId, int from, int size);

    /**
     * Получить запрос с ответами на него
     */
    ItemRequestWithResponsesDto getItemRequestWithResponses(long itemRequestId);
}
