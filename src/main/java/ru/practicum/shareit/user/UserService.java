package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.exceptions.UserNotFoundException;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Получить пользователя по id
     *
     * @param id идентификатор пользвателя
     * @return найденного пользователя
     * @throws EntityNotFoundException если пользователь по id не найден
     */
    public User getUserById(long id) {
        log.info("Get user by id:{}", id);

        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found.", id))
        );
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * Добавить пользователя
     *
     * @param user пользователь
     * @return пользователя с сгенерированным полем id
     */
    public User addUser(User user) {
        log.info("Add {}", user);

        return userRepository.save(user);
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

        User user = userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException(String.format("User with id:%s not found.", id))
        );

        if (updatedUser.getName() != null) {
            user.setName(updatedUser.getName());
        }

        if (updatedUser.getEmail() != null) {
            user.setEmail(updatedUser.getEmail());
        }

        return user;
    }

    /**
     * Удалить пользователя
     *
     * @param id идентификатор пользователя
     * @throws UserNotFoundException если пользователь с переданным id не найден в хранилище
     */
    public void deleteUser(long id) {
        log.info("Delete user with id:{}", id);

        userRepository.deleteById(id);
    }
}
