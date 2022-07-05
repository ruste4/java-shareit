package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    void deleteById(long id);

    User findById(long id);

    List<User> getAll();

    Optional<User> findByEmail(String email);
}
