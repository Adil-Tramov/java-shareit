package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Пользователь с ID " + userId + " не найден"));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Бронирование с ID " + bookingId + " не найдено"));
    }

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingRequestDto bookingRequestDto) {
        if (bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart()) ||
                bookingRequestDto.getEnd().equals(bookingRequestDto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата окончания бронирования должна быть позже даты начала");
        }

        User booker = getUserOrThrow(userId);
        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Вещь с ID " + bookingRequestDto.getItemId() + " не найдена"));

        if (item.getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Вещь с ID " + item.getId() + " недоступна для бронирования");
        }

        if (bookingRepository.existsOverlappingBooking(item.getId(),
                bookingRequestDto.getStart(), bookingRequestDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Указанный период уже забронирован");
        }

        Booking booking = new Booking();
        booking.setStart(bookingRequestDto.getStart());
        booking.setEnd(bookingRequestDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Только владелец вещи может подтверждать бронирование");
        }

        if (!booking.getStatus().equals(Status.WAITING)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Бронирование уже обработано");
        }

        booking.setStatus(approved ? Status.APPROVED : Status.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Просмотр бронирования доступен только автору или владельцу вещи");
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
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStatus().equals(Status.WAITING))
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByBookerId(userId, SORT_BY_START_DESC)
                        .stream()
                        .filter(b -> b.getStatus().equals(Status.REJECTED))
                        .collect(Collectors.toList());
                break;
            default:
                bookings = new ArrayList<>();
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, BookingState state) {
        getUserOrThrow(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.findAllByItemOwnerId(userId, SORT_BY_START_DESC);

        switch (state) {
            case CURRENT:
                bookings = bookings.stream()
                        .filter(b -> b.getStart().isBefore(now) && b.getEnd().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookings.stream()
                        .filter(b -> b.getEnd().isBefore(now))
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookings.stream()
                        .filter(b -> b.getStart().isAfter(now))
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookings.stream()
                        .filter(b -> b.getStatus().equals(Status.WAITING))
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookings.stream()
                        .filter(b -> b.getStatus().equals(Status.REJECTED))
                        .collect(Collectors.toList());
                break;
            default:
                break;
        }

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}