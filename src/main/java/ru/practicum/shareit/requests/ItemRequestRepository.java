package ru.practicum.shareit.requests;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequesterIdOrderByCreatedDesc(long requesterId);

    @Query("SELECT i FROM Item i WHERE i.request=:request")
    List<Item> getAllItemByRequestId(@Param("request") ItemRequest request);
}
