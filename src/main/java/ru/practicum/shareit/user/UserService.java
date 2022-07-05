package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exceptions.UserAlreadyExistException;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

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
        User user = userStorage.findById(id);

        if (user == null) {
            throw new UserNotFoundException("User with id:" + id + " not found.");
        }

        return user;
    }

    /**
     * Добавить пользователя
     *
     * @param user пользователь
     * @return пользователя с сгенерированным полем id
     * @throws UserAlreadyExistException если в хранилище найдется пользователь с одинаковым полем email
     */
    public User addUser(User user) {
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
    public User updateUser(User updatedUser) {
        User user = userStorage.findById(updatedUser.getId());

        if (user == null) {
            throw new UserNotFoundException("User with id:" + updatedUser.getId() + " not found.");
        }

        user.setName(updatedUser.getName());

        return userStorage.updateUser(user);
    }

    /**
     * Удалить пользователя
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь с переданным id не найден в хранилище
     */
    public void deleteUser(long id) {
        User userInStorage = userStorage.findById(id);

        if (userInStorage == null) {
            throw new UserNotFoundException("User with id:" + id + " not found.");
        }

        userStorage.deleteById(id);
    }
}
