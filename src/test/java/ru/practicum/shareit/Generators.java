package ru.practicum.shareit;

import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

public class Generators {

    private static final AtomicLong USER_NUMBER_HOLDER = new AtomicLong();

    public static final Supplier<User> USER_SUPPLIER = () -> {
        User newUser = new User();
        newUser.setName("User" + USER_NUMBER_HOLDER.incrementAndGet());
        newUser.setEmail(String.format("user%s@mail.ru", USER_NUMBER_HOLDER.get()));

        return newUser;
    };

    private static final AtomicLong ITEM_NUMBER_HOLDER = new AtomicLong();

    public static final Supplier<Item> ITEM_SUPPLIER = () -> {
        User itemOwner = new User(
                null,
                String.format("Owner for item%s", ITEM_NUMBER_HOLDER.incrementAndGet()),
                String.format("item%sOwner@mail.ru", ITEM_NUMBER_HOLDER.get()));

        Item newItem = new Item();
        newItem.setName(String.format("Item%s name", ITEM_NUMBER_HOLDER.get()));
        newItem.setDescription(String.format("Item%s description", ITEM_NUMBER_HOLDER.get()));
        newItem.setAvailable(true);
        newItem.setOwner(itemOwner);

        return newItem;
    };
}
