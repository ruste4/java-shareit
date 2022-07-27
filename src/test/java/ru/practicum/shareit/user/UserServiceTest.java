package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import javax.validation.ConstraintViolationException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceTest {

    private static AtomicLong userNumberHolder;

    @Autowired
    private UserService userService;

    @BeforeAll
    public static void beforeAllUserServiceTests() {
        userNumberHolder = new AtomicLong();
    }

    private final Supplier<User> newUser = () -> {
        User newUser = new User();
        newUser.setName("User" + userNumberHolder.incrementAndGet());
        newUser.setEmail(String.format("user%s@mail.ru", userNumberHolder.get()));

        return newUser;
    };

    @Test
    public void createUserTest() {
        User user = newUser.get();

        assertAll(
                () -> assertDoesNotThrow(() -> userService.addUser(user), "User create"),
                () -> assertNotNull(user.getId(), "User id is not null"),
                () -> assertThrows(
                        DataIntegrityViolationException.class,
                        () -> {
                            user.setId(null);
                            userService.addUser(user);
                        },
                        "User create duplicate email"
                ),
                () -> assertThrows(
                        DataIntegrityViolationException.class,
                        () -> {
                            user.setEmail(null);
                            userService.addUser(user);
                        },
                        "User create fail no email"
                ),
                () -> assertThrows(
                        ConstraintViolationException.class,
                        () -> {
                            user.setEmail("user.com");
                            userService.addUser(user);
                        },
                        "User create fail invalid email"
                )
        );
    }

    @Test
    public void userUpdateTest() {
        User user1 = newUser.get();
        User user2 = newUser.get();
        userService.addUser(user1);

        User updatedUser = new User(null, "update", "update@user.com");
        User updateUserName = new User(null, "updateName", null);
        User updateUserEmail = new User(null, null, "updateName@user.com");
        User updateUserEmailExists = new User(null, null, user2.getEmail());

        assertAll(
                () -> assertDoesNotThrow(() -> userService.updateUser(user1.getId(), updatedUser), "User update"),
                () -> userService.addUser(user2),
                () -> assertEquals(
                        userService.updateUser(user1.getId(), updateUserName).getName(),
                        "updateName",
                        "User name update"
                ),
                () -> assertEquals(
                        userService.updateUser(user1.getId(), updateUserEmail).getEmail(),
                        "updateName@user.com",
                        "User name update email"
                ),
                () -> assertThrows(DataIntegrityViolationException.class,
                        () -> userService.updateUser(user1.getId(), updateUserEmailExists),
                        "User name update email exists"
                )
        );

    }

    @Test
    public void userGetTest() {
        User user = newUser.get();
        userService.addUser(user);

        assertAll(
                () -> assertDoesNotThrow(() -> userService.getUserById(user.getId()), "Get user"),
                () -> assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserById(-1),
                        "User get unkonwn"
                )
        );
    }

    @Test
    public void userDeleteTest() {
        User user = newUser.get();
        userService.addUser(user);

        assertAll(
                () -> assertDoesNotThrow(() -> userService.deleteUser(user.getId()), "Delete user"),
                () -> assertDoesNotThrow(() -> {
                    user.setId(null);
                    userService.addUser(user);
                }, "User create after delete")
        );
    }

    @Test
    public void userGetAllTest() {
        assertAll(
                () -> assertTrue(userService.getAll().size() > 0)
        );
    }
}