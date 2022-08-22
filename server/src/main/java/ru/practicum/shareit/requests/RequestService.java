package ru.practicum.shareit.requests;

import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.user.User;

import java.util.List;

public interface RequestService {
    /**
     * Добавить запрос
     */
    ItemRequestDto addItemRequest(ItemRequestCreateDto itemRequestCreateDto, long userId);

    /**
     * Получить все запросы текущего пользователя
     */
    List<ItemRequest> getAllItemRequestsByRequester(User requester);

    /**
     * Получить список запросов текущего пользователя вместе с ответами
     */
    List<ItemRequestWithResponsesDto> getAllItemRequestsWithResponsesCurrentUser(long currentUserId);

    /**
     * Получить запрос по id вместе с ответами
     */
    ItemRequestWithResponsesDto getItemRequestWithResponsesById(long id, long userId);

    /**
     * Получить запрос по id
     */
    ItemRequest getItemRequestById(long id);

    /**
     * Получить список запросов, созданные другими пользователями
     *
     * @param from   - индекс первого элемента (для пагинации)
     * @param size   - количество элементов отображения
     * @param userId
     */
    List<ItemRequestWithResponsesDto> getAllItemRequests(int from, int size, long userId);


}
