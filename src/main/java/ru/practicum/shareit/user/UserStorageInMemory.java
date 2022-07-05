package ru.practicum.shareit.user;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class UserStorageInMemory implements UserStorage {

    private final Map<Long, User> storage = new HashMap<>();
    private final AtomicLong idHolder = new AtomicLong();

    @Override
    public User addUser(User user) {
        user.setId(idHolder.incrementAndGet());

        storage.put(user.getId(), user);

        return user;
    }

    @Override
    public User updateUser(User user) {
        storage.put(user.getId(), user);

        return user;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public User findById(long id) {
        return storage.get(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return storage.values().stream()
                .filter(u -> u.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<User> getAll() {
        return List.copyOf(storage.values());
    }
}
