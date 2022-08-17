package ru.practicum.shareit.item;

import ru.practicum.shareit.user.User;

import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@StaticMetamodel(Item.class)
public class Item_ {
    public static volatile SingularAttribute<Item, User> owner;
}
