package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.shareit.user.exceptions.UserAlreadyExistException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {

    private static AtomicLong userIdHolder;

    @Autowired
    private UserService userService;

    private final Supplier<User> newUser = () -> User.builder()
            .id(userIdHolder.incrementAndGet())
            .name("User" + userIdHolder.get())
            .email("user" + userIdHolder.get() + "@mail.ru")
            .build();
    ;

    @BeforeAll
    public static void beforeAllUserServiceTests() {
        userIdHolder = new AtomicLong();
    }

    @Test
    public void shouldBeSuccessfulAdditionNewUser() {
        User user = newUser.get();

        assertDoesNotThrow(() -> userService.addUser(user));
    }

    @Test
    public void shouldBeAlreadyExistExceptionWhenDuplicatingEmailAtAdding() {
        User user1 = newUser.get();
        User user2 = newUser.get();

        user2.setEmail(user1.getEmail());

        assertThrows(UserAlreadyExistException.class, () -> {
            userService.addUser(user1);
            userService.addUser(user2);
        });
    }

    @Test
    public void shouldBeSuccessfulGetUser() {
        User user = newUser.get();

        userService.addUser(user);

        assertEquals(userService.getUserById(user.getId()), user);
    }

    @Test
    public void shouldByUserNotFoundExceptionWhenUserNotExist() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(-1L));
    }

    @Test
    public void shouldBeSuccessfulUpdatingUser() {
        User user = newUser.get();
        User updatedUser = newUser.get();
        String userName = "Updated user name";

        userService.addUser(user);
        updatedUser.setId(user.getId());
        updatedUser.setName(userName);
        userService.updateUser(updatedUser.getId(), updatedUser);

        assertEquals(userService.getUserById(user.getId()).getName(), userName);
    }

    @Test
    public void shouldBeUserNotFoundExceptionWhenUpdatingNotExistUser() {
        User user = newUser.get();
        user.setId(-1L);

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(user.getId(), user));
    }

    @Test
    public void shouldBeUserAlreadyExistExceptionWhenEmailExist() {
        User user1 = newUser.get();
        User user2 = newUser.get();
        String user1Email = "alreadyExist@email.ru";
        user1.setEmail(user1Email);

        userService.addUser(user1);
        userService.addUser(user2);

        User updatedUser2 = newUser.get();
        updatedUser2.setId(user2.getId());
        updatedUser2.setName(user2.getName());
        updatedUser2.setEmail(user1Email);

        assertThrows(UserAlreadyExistException.class, () -> {
            userService.updateUser(updatedUser2.getId(), updatedUser2);
        });
    }

    @Test
    public void shouldBeSuccessfulDeleteUser() {
        User user = newUser.get();

        userService.addUser(user);
        userService.deleteUser(user.getId());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(user.getId()));
    }

    @Test
    public void shouldBeUserNotFoundExceptionWhenUserNotExist() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(-1));
    }
}