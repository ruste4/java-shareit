package ru.practicum.shareit.requests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Generators;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.requests.dto.ItemRequestCreateDto;
import ru.practicum.shareit.requests.dto.ItemRequestDto;
import ru.practicum.shareit.requests.dto.ItemRequestWithResponsesDto;
import ru.practicum.shareit.user.User;

import javax.persistence.TypedQuery;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@Transactional
@AutoConfigureTestEntityManager
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class RequestServiceImpTest {
    @Autowired
    private final RequestService requestService;

    @Autowired
    private final TestEntityManager testEntityManager;

    @Test
    public void addItemRequest() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description = "Item request by addItemRequest testing";

        ItemRequestCreateDto itemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description)
                .build();

        requestService.addItemRequest(itemRequestCreateDto);

        TypedQuery<ItemRequest> query = testEntityManager.getEntityManager().createQuery(
                "SELECT iq FROM ItemRequest iq where iq.description = :description",
                ItemRequest.class
        );

        ItemRequest itemRequest = query.setParameter("description", description).getSingleResult();

        assertAll(
                () -> assertNotNull(itemRequest.getId()),
                () -> assertEquals(itemRequest.getRequester().getId(), itemRequestCreateDto.getRequesterId()),
                () -> assertEquals(itemRequest.getDescription(), itemRequestCreateDto.getDescription())
        );
    }

    @Test
    public void getAllItemRequestsByRequesterId() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description1 = "Description by first request";

        ItemRequestCreateDto firstItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description1)
                .build();

        String description2 = "Description by second request";
        ItemRequestCreateDto secondItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description2)
                .build();

        String description3 = "Description by third request";
        ItemRequestCreateDto thirdItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description3)
                .build();

        requestService.addItemRequest(firstItemRequestCreateDto);
        requestService.addItemRequest(secondItemRequestCreateDto);
        requestService.addItemRequest(thirdItemRequestCreateDto);

        List<ItemRequest> requestList = requestService.getAllItemRequestsByRequesterId(requesterId);

        List<ItemRequest> foundedRequests = testEntityManager.getEntityManager().createQuery(
                "SELECT iq FROM ItemRequest iq where iq.requester = :requester",
                ItemRequest.class
        ).setParameter("requester", requester).getResultList().stream()
                .sorted(Comparator.comparing(ItemRequest::getCreated).reversed())
                .collect(Collectors.toList());

        assertEquals(requestList, foundedRequests);
    }

    @Test
    public void getAllItemRequestsWithResponsesCurrentUser() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description1 = "Description by first request";

        ItemRequestCreateDto firstItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description1)
                .build();

        String description2 = "Description by second request";
        ItemRequestCreateDto secondItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description2)
                .build();

        String description3 = "Description by third request";
        ItemRequestCreateDto thirdItemRequestCreateDto = ItemRequestCreateDto.builder()
                .requesterId(requesterId)
                .description(description3)
                .build();

        ItemRequestDto itemRequestDto1 = requestService.addItemRequest(firstItemRequestCreateDto);
        ItemRequestDto itemRequestDto2 = requestService.addItemRequest(secondItemRequestCreateDto);
        ItemRequestDto itemRequestDto3 = requestService.addItemRequest(thirdItemRequestCreateDto);

        // Добавляем вещи отвечающие запросам

        Item item1 = Generators.ITEM_SUPPLIER.get();
        item1.setRequest(testEntityManager.find(ItemRequest.class, itemRequestDto1.getId()));
        testEntityManager.persist(item1.getOwner());
        testEntityManager.persist(item1);

        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setRequest(testEntityManager.find(ItemRequest.class, itemRequestDto1.getId()));
        testEntityManager.persist(item2.getOwner());
        testEntityManager.persist(item2);

        Item item3 = Generators.ITEM_SUPPLIER.get();
        item3.setRequest(testEntityManager.find(ItemRequest.class, itemRequestDto2.getId()));
        testEntityManager.persist(item3.getOwner());
        testEntityManager.persist(item3);

        List<ItemRequestWithResponsesDto> serviceResult =
                requestService.getAllItemRequestsWithResponsesCurrentUser(requester.getId());

        assertAll(
                () -> assertEquals(
                        serviceResult.get(0).getId(),
                        itemRequestDto3.getId(),
                        String.format("Первый запрос должен быть %s", itemRequestDto3)
                ),
                () -> assertEquals(
                        serviceResult.get(0).getResponses().size(), 0, "Ответов на запрос не должно быть"
                ),
                () -> assertEquals(
                        serviceResult.get(1).getId(),
                        itemRequestDto2.getId(),
                        String.format("Второй запрос должен быть %s", itemRequestDto2)
                ),
                () -> assertEquals(
                        serviceResult.get(1).getResponses().size(), 1, "Должен быть один ответ для запроса"
                ),
                () -> assertEquals(
                        serviceResult.get(1).getResponses().get(0).getItemId(),
                        item3.getId(),
                        String.format("Ответом на запрос должен быть %s", item3)
                ),
                () -> assertEquals(
                        serviceResult.get(2).getId(),
                        itemRequestDto1.getId(),
                        String.format("Трейтий запрос должен быть %s", itemRequestDto1)
                ),
                () -> assertEquals(
                        serviceResult.get(2).getResponses().size(), 2, "Должно быть два ответа на запрос"
                ),
                () -> assertEquals(
                        serviceResult.get(2).getResponses().get(0).getItemId(),
                        item1.getId(),
                        String.format("Ответом на запрос должен быть %s", item1)
                ),
                () -> assertEquals(
                        serviceResult.get(2).getResponses().get(1).getItemId(),
                        item2.getId(),
                        String.format("Ответом на запрос должен быть %s", item2)
                )
        );
    }
}