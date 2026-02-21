package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByBookerId(Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId")
    List<Booking> findAllByItemOwnerId(@Param("ownerId") Long ownerId, Sort sort);

    boolean existsByBookerIdAndItemIdAndEndBeforeAndStatus(
            Long bookerId, Long itemId, LocalDateTime end, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId,
                                  @Param("now") LocalDateTime now,
                                  Sort sort);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByBookerIdAndEndBefore(
            Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findAllByBookerIdAndStartAfter(
            Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findAllByBookerIdAndStatus(
            Long bookerId, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start < :now AND b.end > :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId,
                                       @Param("now") LocalDateTime now,
                                       Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.end < :now")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId,
                                    @Param("now") LocalDateTime now,
                                    Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.start > :now")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId,
                                      @Param("now") LocalDateTime now,
                                      Sort sort);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.status = :status")
    List<Booking> findByOwnerIdAndStatus(@Param("ownerId") Long ownerId,
                                         @Param("status") BookingStatus status,
                                         Sort sort);
}