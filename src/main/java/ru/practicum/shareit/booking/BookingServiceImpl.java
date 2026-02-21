package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        User booker = getUserOrThrow(userId);

        if (bookingDto.getItem() == null || bookingDto.getItem().getId() == null) {
            throw new ValidationException("ID вещи должен быть указан");
        }

        Item item = getItemOrThrow(bookingDto.getItem().getId());

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        Booking booking = BookingMapper.toBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может подтверждать бронирование");
        }

        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Нет доступа к просмотру бронирования");
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllByBookerIdAndStartBeforeAndEndAfter(
                        userId, now, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerIdAndEndBefore(
                        userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerIdAndStartAfter(
                        userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        getUserOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByItemOwnerId(userId, SORT_BY_START_DESC);
                break;
            case CURRENT:
                bookings = bookingRepository.findCurrentByOwnerId(userId, now, SORT_BY_START_DESC);
                break;
            case PAST:
                bookings = bookingRepository.findPastByOwnerId(userId, now, SORT_BY_START_DESC);
                break;
            case FUTURE:
                bookings = bookingRepository.findFutureByOwnerId(userId, now, SORT_BY_START_DESC);
                break;
            case WAITING:
                bookings = bookingRepository.findByOwnerIdAndStatus(
                        userId, BookingStatus.WAITING, SORT_BY_START_DESC);
                break;
            case REJECTED:
                bookings = bookingRepository.findByOwnerIdAndStatus(
                        userId, BookingStatus.REJECTED, SORT_BY_START_DESC);
                break;
            default:
                throw new ValidationException("Unknown state: " + state);
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с ID " + bookingId + " не найдено"));
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Даты начала и окончания должны быть указаны");
        }
        if (!end.isAfter(start)) {
            throw new ValidationException("Дата окончания должна быть позже даты начала");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала не может быть в прошлом");
        }
    }
}