package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.Generators;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ItemNotFoundException;
import ru.practicum.shareit.item.exceptions.UserNotOwnerItemException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import javax.validation.ConstraintViolationException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@AutoConfigureTestEntityManager
@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceTest {
    @Autowired
    private final ItemService itemService;

    @Autowired
    private final TestEntityManager testEntityManager;

    @BeforeEach
    public void beforeEachItemServiceTest() {
        testEntityManager.clear();
    }

    @Test
    public void createItem() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        testEntityManager.flush();

        Item createdItem = itemService.addItem(ownerId, item);
        Item foundItem = testEntityManager.find(Item.class, item.getId());

        assertEquals(createdItem, foundItem);
    }

    @Test
    public void itemCreateFailedByWrongUserId() {
        Item item = Generators.ITEM_SUPPLIER.get();

        assertThrows(UserNotFoundException.class, () -> itemService.addItem(10, item));
    }

    @Test
    public void itemCreateWithEmptyName() {
        Item item = Generators.ITEM_SUPPLIER.get();
        item.setName("");
        Long ownerid = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        testEntityManager.flush();

        assertThrows(ConstraintViolationException.class, () -> itemService.addItem(ownerid, item));
    }

    @Test
    public void itemCreateWithEmptyDescription() {
        Item item = Generators.ITEM_SUPPLIER.get();
        item.setDescription("");
        Long ownerid = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        testEntityManager.flush();

        assertThrows(ConstraintViolationException.class, () -> itemService.addItem(ownerid, item));
    }

    @Test
    public void itemUpdate() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();

        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .name("Дрель+")
                .description("Аккумуляторная дрель")
                .available(false)
                .build();

        assertAll(
                () -> assertDoesNotThrow(() -> itemService.updateItem(ownerId, updatedItemDto)),
                () -> assertEquals(testEntityManager.find(Item.class, itemId).getName(), updatedItemDto.getName())
        );
    }

    @Test
    public void itemUpdateWithOtherUser() {
        Item item = Generators.ITEM_SUPPLIER.get();
        testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        long otherUserId = testEntityManager.persistAndGetId(Generators.USER_SUPPLIER.get(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();

        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .name("Дрель")
                .description("Простая дрель")
                .available(false)
                .build();

        assertThrows(UserNotOwnerItemException.class, () -> itemService.updateItem(otherUserId, updatedItemDto));
    }

    @Test
    public void itemUpdateAvailable() {
        Item item = Generators.ITEM_SUPPLIER.get();
        item.setAvailable(false);
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();

        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .available(true)
                .build();

        assertAll(
                () -> assertFalse(testEntityManager.find(Item.class, itemId).isAvailable()),
                () -> assertDoesNotThrow(() -> itemService.updateItem(ownerId, updatedItemDto)),
                () -> assertTrue(testEntityManager.find(Item.class, itemId).isAvailable())
        );
    }

    @Test
    public void itemUpdateDescription() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();
        String updatedDescription = "updated description";


        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .description(updatedDescription)
                .build();

        assertAll(
                () -> assertNotEquals(testEntityManager.find(Item.class, itemId).getDescription(), updatedDescription),
                () -> assertDoesNotThrow(() -> itemService.updateItem(ownerId, updatedItemDto)),
                () -> assertEquals(testEntityManager.find(Item.class, itemId).getDescription(), updatedDescription)
        );
    }

    @Test
    public void itemUpdateName() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();
        String updatedName = "updated name";


        ItemDto updatedItemDto = ItemDto.builder()
                .id(itemId)
                .name(updatedName)
                .build();

        assertAll(
                () -> assertNotEquals(testEntityManager.find(Item.class, itemId).getName(), updatedName),
                () -> assertDoesNotThrow(() -> itemService.updateItem(ownerId, updatedItemDto)),
                () -> assertEquals(testEntityManager.find(Item.class, itemId).getName(), updatedName)
        );
    }

    @Test
    public void itemGet() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        Long itemId = testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();

        assertEquals(itemService.getItemById(itemId, ownerId).getName(), item.getName());
    }

    @Test
    public void itemGetUnknown() {
        Item item = Generators.ITEM_SUPPLIER.get();
        Long ownerId = testEntityManager.persistAndGetId(item.getOwner(), Long.class);
        testEntityManager.persistAndGetId(item, Long.class);
        testEntityManager.flush();

        assertThrows(ItemNotFoundException.class, () -> itemService.getItemById(100, ownerId));
    }

    @Test
    public void itemGetAll() {
        Item item1 = Generators.ITEM_SUPPLIER.get();
        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setOwner(item1.getOwner());

        Long ownerId = testEntityManager.persistAndGetId(item1.getOwner(), Long.class);
        testEntityManager.persist(item1);
        testEntityManager.persist(item2);
        testEntityManager.flush();

        assertEquals(itemService.getAllByOwnerId(ownerId).size(), 2);
    }

    @Test
    public void itemSearchByDescription() {
        String searchTxt = "аккУМУляторная";
        Item item1 = Generators.ITEM_SUPPLIER.get();
        item1.setDescription("Аккумуляторная батарея");
        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setName("Аккумуляторная дрель");
        Item item3 = Generators.ITEM_SUPPLIER.get();

        testEntityManager.persist(item1.getOwner());
        testEntityManager.persist(item1);
        testEntityManager.persist(item2.getOwner());
        testEntityManager.persist(item2);
        testEntityManager.persist(item3.getOwner());
        testEntityManager.persist(item3);
        testEntityManager.flush();

        assertEquals(itemService.searchByNameAndDescription(searchTxt).size(), 2);
    }

    @Test
    public void itemSearchEmpty() {
        String searchTxt = "";
        Item item1 = Generators.ITEM_SUPPLIER.get();
        item1.setDescription("Аккумуляторная батарея");
        Item item2 = Generators.ITEM_SUPPLIER.get();
        item2.setName("Аккумуляторная дрель");
        Item item3 = Generators.ITEM_SUPPLIER.get();

        testEntityManager.persist(item1.getOwner());
        testEntityManager.persist(item1);
        testEntityManager.persist(item2.getOwner());
        testEntityManager.persist(item2);
        testEntityManager.persist(item3.getOwner());
        testEntityManager.persist(item3);
        testEntityManager.flush();

        assertEquals(itemService.searchByNameAndDescription(searchTxt).size(), 0);
    }
}