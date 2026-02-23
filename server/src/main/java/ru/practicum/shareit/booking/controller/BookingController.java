package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.dto.BookingListDto;
import ru.practicum.shareit.booking.service.BookingService;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingService bookingService;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingDtoResponse> createBooking(
            @RequestHeader(USER_ID_HEADER) Long bookerId,
            @RequestBody BookingDto bookingDto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(bookingService.createBooking(bookerId, bookingDto));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> approveBooking(
            @RequestHeader(USER_ID_HEADER) Long ownerId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {

        System.out.println("Подтверждение бронирования: bookingId=" + bookingId + ", approved=" + approved);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.approveBooking(ownerId, bookingId, approved));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDtoResponse> getBookingByIdForOwnerAndBooker(
            @PathVariable Long bookingId,
            @RequestHeader(USER_ID_HEADER) Long userId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getBookingByIdForOwnerAndBooker(bookingId, userId));
    }

    @GetMapping
    public ResponseEntity<BookingListDto> getAllBookingsForUser(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getAllBookingsForUser(pageRequest, userId, state));
    }

    @GetMapping("/owner")
    public ResponseEntity<BookingListDto> getAllBookingsForItemsUser(
            @RequestHeader(USER_ID_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size) {

        int page = from / size;
        PageRequest pageRequest = PageRequest.of(page, size);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(bookingService.getAllBookingsForItemsUser(pageRequest, userId, state));
    }
}