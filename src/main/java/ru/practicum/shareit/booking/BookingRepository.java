package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    List<Booking> findByBookerIdAndItemIdAndStatus(
            Long bookerId,
            Long itemId,
            Status status
    );

    List<Booking> findByBookerIdAndItemIdAndStatusOrderByEndDesc(
            Long bookerId,
            Long itemId,
            Status status
    );

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.booker.id = :userId " +
            "AND b.item.id = :itemId " +
            "AND b.end < :now " +
            "AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED")
    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatusApproved(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start < :now " +
            "AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start > :now " +
            "AND b.status = ru.practicum.shareit.booking.model.Status.APPROVED " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END " +
            "FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status IN (ru.practicum.shareit.booking.model.Status.APPROVED, ru.practicum.shareit.booking.model.Status.WAITING) " +
            "AND (:start < b.end AND :end > b.start)")
    boolean existsOverlappingBooking(
            @Param("itemId") Long itemId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}