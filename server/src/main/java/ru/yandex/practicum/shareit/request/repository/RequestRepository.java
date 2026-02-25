package ru.yandex.practicum.shareit.request.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.shareit.request.model.ItemRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorIdOrderByCreatedDesc(Long requestorId);

    @Query("SELECT r FROM ItemRequest r " +
            "WHERE r.requestor.id != :userId " +
            "ORDER BY r.created DESC")
    List<ItemRequest> findAllExceptUser(@Param("userId") Long userId, Pageable pageable);
}