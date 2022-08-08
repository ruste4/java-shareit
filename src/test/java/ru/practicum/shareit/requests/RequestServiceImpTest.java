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

        ItemRequestCreateDto itemRequestCreateDto = new ItemRequestCreateDto(description);

        requestService.addItemRequest(itemRequestCreateDto, requesterId);

        TypedQuery<ItemRequest> query = testEntityManager.getEntityManager().createQuery(
                "SELECT iq FROM ItemRequest iq where iq.description = :description",
                ItemRequest.class
        );

        ItemRequest itemRequest = query.setParameter("description", description).getSingleResult();

        assertAll(
                () -> assertNotNull(itemRequest.getId()),
                () -> assertEquals(itemRequest.getRequester().getId(), requesterId),
                () -> assertEquals(itemRequest.getDescription(), itemRequestCreateDto.getDescription())
        );
    }

    @Test
    public void getAllItemRequestsByRequesterId() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description1 = "Description by first request";

        ItemRequestCreateDto firstItemRequestCreateDto = new ItemRequestCreateDto(description1);

        String description2 = "Description by second request";
        ItemRequestCreateDto secondItemRequestCreateDto = new ItemRequestCreateDto(description2);

        String description3 = "Description by third request";
        ItemRequestCreateDto thirdItemRequestCreateDto = new ItemRequestCreateDto(description3);

        requestService.addItemRequest(firstItemRequestCreateDto, requesterId);
        requestService.addItemRequest(secondItemRequestCreateDto, requesterId);
        requestService.addItemRequest(thirdItemRequestCreateDto, requesterId);

        List<ItemRequest> requestList = requestService.getAllItemRequestsByRequester(
                testEntityManager.find(User.class, requesterId)
        );

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

        ItemRequestCreateDto firstItemRequestCreateDto = new ItemRequestCreateDto(description1);

        String description2 = "Description by second request";
        ItemRequestCreateDto secondItemRequestCreateDto = new ItemRequestCreateDto(description2);

        String description3 = "Description by third request";
        ItemRequestCreateDto thirdItemRequestCreateDto = new ItemRequestCreateDto(description3);

        ItemRequestDto itemRequestDto1 = requestService.addItemRequest(firstItemRequestCreateDto, requesterId);
        ItemRequestDto itemRequestDto2 = requestService.addItemRequest(secondItemRequestCreateDto, requesterId);
        ItemRequestDto itemRequestDto3 = requestService.addItemRequest(thirdItemRequestCreateDto, requesterId);

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
                        serviceResult.get(1).getResponses().get(0).getId(),
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
                        serviceResult.get(2).getResponses().get(0).getId(),
                        item1.getId(),
                        String.format("Ответом на запрос должен быть %s", item1)
                ),
                () -> assertEquals(
                        serviceResult.get(2).getResponses().get(1).getId(),
                        item2.getId(),
                        String.format("Ответом на запрос должен быть %s", item2)
                )
        );
    }

    @Test
    public void getAllItemRequests() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description1 = "Description by first request";

        ItemRequestCreateDto firstItemRequestCreateDto = new ItemRequestCreateDto(description1);

        String description2 = "Description by second request";
        ItemRequestCreateDto secondItemRequestCreateDto = new ItemRequestCreateDto(description2);

        String description3 = "Description by third request";
        ItemRequestCreateDto thirdItemRequestCreateDto = new ItemRequestCreateDto(description3);

        String description4 = "Description by fourth request";
        ItemRequestCreateDto fourthItemRequestCreateDto = new ItemRequestCreateDto(description4);

        ItemRequestDto itemRequestDto1 = requestService.addItemRequest(firstItemRequestCreateDto, requesterId);
        ItemRequestDto itemRequestDto2 = requestService.addItemRequest(secondItemRequestCreateDto, requesterId);
        ItemRequestDto itemRequestDto3 = requestService.addItemRequest(thirdItemRequestCreateDto, requesterId);
        ItemRequestDto itemRequestDto4 = requestService.addItemRequest(fourthItemRequestCreateDto, requesterId);

        List<ItemRequestDto> serviceResult = requestService.getAllItemRequests(0, 3);

        assertAll(
                "Проверка from=0, size=4",
                () -> assertEquals(serviceResult.size(), 3, "Количестов элементов в результате 3"),
                () -> assertEquals(
                        serviceResult.get(0).getId(),
                        itemRequestDto4.getId(),
                        String.format("Первый элемент должен быть %s", itemRequestDto4)
                ),
                () -> assertEquals(
                        serviceResult.get(1).getId(),
                        itemRequestDto3.getId(),
                        String.format("Второй эелемент должен быть %s", itemRequestDto3)
                ),
                () -> assertEquals(
                        serviceResult.get(2).getId(),
                        itemRequestDto2.getId(),
                        String.format("Трейтий элемент должен быть %s", itemRequestDto2)
                )
        );

        List<ItemRequestDto> serviceResultSecondTest = requestService.getAllItemRequests(1, 2);

        assertAll(
                "Проверка from=1, size=2",
                () -> assertEquals(
                        serviceResultSecondTest.size(), 2, "Количество элементов в результате должно быть 2"
                ),
                () -> assertEquals(
                        serviceResultSecondTest.get(0).getId(),
                        itemRequestDto2.getId(),
                        String.format("Первый элемент должен быть %s", itemRequestDto2)
                ),
                () -> assertEquals(
                        serviceResultSecondTest.get(1).getId(),
                        itemRequestDto1.getId(),
                        String.format("Второй элемент должен быть %s", itemRequestDto1)
                )
        );
    }

    @Test
    public void getItemRequestWithResponsesById() {
        User requester = Generators.USER_SUPPLIER.get();
        Long requesterId = testEntityManager.persistAndGetId(requester, Long.class);
        String description1 = "Description by first request";

        ItemRequestCreateDto firstItemRequestCreateDto = new ItemRequestCreateDto(description1);

        ItemRequestDto itemRequestDto1 = requestService.addItemRequest(firstItemRequestCreateDto, requesterId);

        Item item1 = Generators.ITEM_SUPPLIER.get();
        item1.setRequest(testEntityManager.find(ItemRequest.class, itemRequestDto1.getId()));
        testEntityManager.persist(item1.getOwner());
        Long idForItem1 = testEntityManager.persistAndGetId(item1, Long.class);

        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setRequest(testEntityManager.find(ItemRequest.class, itemRequestDto1.getId()));
        testEntityManager.persist(item2.getOwner());
        Long idForItem2 = testEntityManager.persistAndGetId(item2, Long.class);

        ItemRequestWithResponsesDto itemRequestWithResponsesDto = requestService
                .getItemRequestWithResponsesById(itemRequestDto1.getId());

        assertAll(
                () -> assertEquals(itemRequestWithResponsesDto.getResponses().size(), 2),
                () -> assertEquals(
                        itemRequestWithResponsesDto.getResponses().get(0).getId(),
                        idForItem1,
                        String.format("id для первого ответа должен быть %s", idForItem1)
                ),
                () -> assertEquals(
                        itemRequestWithResponsesDto.getResponses().get(1).getId(),
                        idForItem2,
                        String.format("id для второго ответа должен быть %s", idForItem2)
                )
        );
    }
}