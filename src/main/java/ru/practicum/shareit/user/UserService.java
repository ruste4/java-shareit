package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exceptions.UserAlreadyExistException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    /**
     * Получить пользователя по id
     *
     * @param id идентификатор пользвателя
     * @return найденного пользователя
     * @throws UserNotFoundException если пользователь по id не найден
     */
    public User getUserById(long id) {
        log.info("Get user by id:{}", id);
        User user = userStorage.findById(id);

        if (user == null) {
            throw new UserNotFoundException("User with id:" + id + " not found.");
        }

        return user;
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    /**
     * Добавить пользователя
     *
     * @param user пользователь
     * @return пользователя с сгенерированным полем id
     * @throws UserAlreadyExistException если в хранилище найдется пользователь с одинаковым полем email
     */
    public User addUser(User user) {
        log.info("Add {}", user);
        if (userStorage.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistException("User with email:" + user.getEmail() + " already exist.");
        }

        userStorage.addUser(user);

        return user;
    }

    /**
     * Обновить пользователя
     *
     * @param updatedUser обновленная версия пользователя
     * @return обновленного пользователя
     * @throws UserNotFoundException если в хранилище нет пользователя с одинаковым полем id с updatedUser
     */
    public User updateUser(long id, User updatedUser) {
        log.info("Update user with email:{} on {}", updatedUser.getEmail(), updatedUser);
        User user = userStorage.findById(id);

        if (user == null) {
            throw new UserNotFoundException("User with id:" + id + " not found.");
        }

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getEmail() != null) {
            Optional<User> finedUserByEmail = userStorage.findByEmail(updatedUser.getEmail());

            if (finedUserByEmail.isPresent() && !finedUserByEmail.get().equals(user)) {
                throw new UserAlreadyExistException("User with email:" + updatedUser.getEmail() + " already exist.");
            }

            user.setEmail(updatedUser.getEmail());
        }

        return userStorage.updateUser(user);
    }

    /**
     * Удалить пользователя
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь с переданным id не найден в хранилище
     */
    public void deleteUser(long id) {
        log.info("Delete user with id:{}", id);
        User userInStorage = userStorage.findById(id);

        if (userInStorage == null) {
            throw new UserNotFoundException("User with id:" + id + " not found.");
        }

        userStorage.deleteById(id);
    }
}
