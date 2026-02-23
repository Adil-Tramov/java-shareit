package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.handler.exception.StateException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookings;
    private final UserRepository users;
    private final ItemRepository items;
    private final BookingMapper mapper;

    @Override
    @Transactional
    public BookingDtoResponse createBooking(Long bookerId, BookingDto bookingDto) {
        System.out.println("Создание бронирования: bookerId=" + bookerId + ", itemId=" + bookingDto.getItemId());

        if (bookingDto.getEnd().isBefore(bookingDto.getStart()) ||
                bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Дата окончания бронирования должна быть позже даты начала");
        }

        Item item = items.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Вещь с id=%d не найдена", bookingDto.getItemId())));

        System.out.println("Найдена вещь: " + item.getName() + ", владелец: " + item.getOwner().getName());

        if (item.getOwner().getId().equals(bookerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Владелец не может забронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Вещь с id=%d недоступна для бронирования", item.getId()));
        }

        User booker = users.findById(bookerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Пользователь с id=%d не найден", bookerId)));

        System.out.println("Найден пользователь: " + booker.getName());

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);

        Booking savedBooking = bookings.save(booking);
        System.out.println("Сохранено бронирование с id=" + savedBooking.getId());

        BookingDtoResponse response = mapToFullDto(savedBooking);
        System.out.println("Ответ DTO: " + response);

        return response;
    }

    @Override
    @Transactional
    public BookingDtoResponse approveBooking(Long ownerId, Long bookingId, boolean approved) {
        System.out.println("Подтверждение бронирования: bookingId=" + bookingId + ", ownerId=" + ownerId + ", approved=" + approved);

        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Бронирование с id=%d не найдено", bookingId)));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%d не является владельцем вещи", ownerId));
        }

        if (booking.getStatus() != Status.WAITING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Невозможно изменить статус бронирования со статусом " + booking.getStatus());
        }

        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }

        Booking updatedBooking = bookings.save(booking);
        System.out.println("Статус изменен на: " + updatedBooking.getStatus());

        return mapToFullDto(updatedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingDtoResponse getBookingByIdForOwnerAndBooker(Long bookingId, Long userId) {
        System.out.println("Запрос бронирования: bookingId=" + bookingId + ", userId=" + userId);

        Booking booking = bookings.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Бронирование с id=%d не найдено", bookingId)));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%d не имеет доступа к бронированию", userId));
        }

        return mapToFullDto(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingListDto getAllBookingsForUser(Pageable pageable, Long userId, String state) {
        System.out.println("Запрос бронирований пользователя: userId=" + userId + ", state=" + state);

        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%d не найден", userId));
        }

        return getListBookings(pageable, state, userId, false);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingListDto getAllBookingsForItemsUser(Pageable pageable, Long userId, String state) {
        System.out.println("Запрос бронирований вещей пользователя: userId=" + userId + ", state=" + state);

        if (!users.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("Пользователь с id=%d не найден", userId));
        }

        if (!items.existsItemByOwnerId(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("У пользователя с id=%d нет зарегистрированных вещей", userId));
        }

        return getListBookings(pageable, state, userId, true);
    }

    private BookingListDto getListBookings(Pageable pageable, String state, Long userId, Boolean isOwner) {
        List<Long> itemsId;
        List<Booking> bookingsList;

        switch (State.fromValue(state.toUpperCase())) {
            case ALL:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInOrderByStartDesc(pageable, itemsId);
                } else {
                    bookingsList = bookings.findAllByBookerIdOrderByStartDesc(pageable, userId);
                }
                break;

            case CURRENT:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                            pageable, itemsId, LocalDateTime.now(), LocalDateTime.now());
                } else {
                    bookingsList = bookings.findAllByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(
                            pageable, userId, LocalDateTime.now(), LocalDateTime.now());
                }
                break;

            case PAST:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInAndEndIsBeforeOrderByStartDesc(
                            pageable, itemsId, LocalDateTime.now());
                } else {
                    bookingsList = bookings.findAllByBookerIdAndEndIsBeforeOrderByStartDesc(
                            pageable, userId, LocalDateTime.now());
                }
                break;

            case FUTURE:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInAndStartIsAfterOrderByStartDesc(
                            pageable, itemsId, LocalDateTime.now());
                } else {
                    bookingsList = bookings.findAllByBookerIdAndStartIsAfterOrderByStartDesc(
                            pageable, userId, LocalDateTime.now());
                }
                break;

            case WAITING:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInAndStatusIsOrderByStartDesc(
                            pageable, itemsId, Status.WAITING);
                } else {
                    bookingsList = bookings.findAllByBookerIdAndStatusIsOrderByStartDesc(
                            pageable, userId, Status.WAITING);
                }
                break;

            case REJECTED:
                if (isOwner) {
                    itemsId = items.findAllItemIdByOwnerId(userId);
                    bookingsList = bookings.findAllByItemIdInAndStatusIsOrderByStartDesc(
                            pageable, itemsId, Status.REJECTED);
                } else {
                    bookingsList = bookings.findAllByBookerIdAndStatusIsOrderByStartDesc(
                            pageable, userId, Status.REJECTED);
                }
                break;

            default:
                throw new StateException("Unknown state: " + state);
        }

        List<BookingDtoResponse> responses = bookingsList.stream()
                .map(this::mapToFullDto)
                .collect(Collectors.toList());

        return BookingListDto.builder()
                .bookings(responses)
                .build();
    }

    private BookingDtoResponse mapToFullDto(Booking booking) {
        return BookingDtoResponse.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .booker(UserShortDto.builder()
                        .id(booking.getBooker().getId())
                        .name(booking.getBooker().getName())
                        .build())
                .item(ItemShortDto.builder()
                        .id(booking.getItem().getId())
                        .name(booking.getItem().getName())
                        .build())
                .build();
    }
}