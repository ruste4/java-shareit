package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {

    @Autowired
    private final ItemService itemService;
    @Autowired
    private final UserService userService;
    private static AtomicLong userIdHolder;
    private static AtomicLong itemIdHolder;

    private final Supplier<User> newUser = () -> User.builder()
            .id(userIdHolder.incrementAndGet())
            .name("User" + userIdHolder.get())
            .email("user" + userIdHolder.get() + "@mail.ru")
            .build();

    private final Supplier<Item> newItem = () -> Item.builder()
            .name("Name Item.id:" + itemIdHolder.incrementAndGet())
            .description("Desc Item.id:" + itemIdHolder.get())
            .available(true)
            .owner(newUser.get())
            .build();

    @BeforeAll
    public static void beforeAllItemServiceImplTests() {
        userIdHolder = new AtomicLong();
        itemIdHolder = new AtomicLong();
    }

    @Test
    public void shouldBeSuccessfulCreateNewItem() {
        Item item = newItem.get();
        userService.addUser(item.getOwner());

        assertDoesNotThrow(() -> itemService.addItem(item.getOwner().getId(), item));
    }

    @Test
    public void shouldBeUserNotFoundWhenCreateNewItemWithNotExistUser() {
        Item item = newItem.get();

        assertThrows(UserNotFoundException.class, () -> itemService.addItem(item.getOwner().getId(), item));
    }

    @Test
    public void shouldBuSuccessfulUpdateItem() {
        Item item = newItem.get();
        userService.addUser(item.getOwner());
        itemService.addItem(item.getOwner().getId(), item);

        String updatedName = item.getName() + " UPDATED";
        Item updatedItem = Item.builder()
                .id(item.getId())
                .name(updatedName)
                .description(item.getDescription())
                .owner(item.getOwner())
                .build();

        itemService.updateItem(updatedItem.getOwner().getId(), ItemMapper.toItemDto(updatedItem));

        Item checkItem = itemService.getItemById(item.getId());
        assertEquals(checkItem.getName(), updatedName);
    }

    @Test
    public void shouldBeUserNotFoundWhenUpdateItemWithNotExistUser() {
        Item item = newItem.get();
        userService.addUser(item.getOwner());
        itemService.addItem(item.getOwner().getId(), item);

        String updatedName = item.getName() + " UPDATED";
        Item updatedItem = Item.builder()
                .id(item.getId())
                .name(updatedName)
                .description(item.getDescription())
                .owner(newUser.get())
                .build();

        assertThrows(UserNotFoundException.class,
                () -> itemService.updateItem(updatedItem.getOwner().getId(), ItemMapper.toItemDto(updatedItem))
        );
    }
}