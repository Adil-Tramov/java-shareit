package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.common.Header;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/bookings")
@Validated
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public Mono<ResponseEntity<Object>> createBooking(
            @RequestHeader(Header.userIdHeader) @Min(1) Long bookerId,
            @Valid @RequestBody BookingDto bookingDto) {
        return bookingClient.createBooking(bookerId, bookingDto);
    }

    @PatchMapping("{bookingId}")
    public Mono<ResponseEntity<Object>> approveBooking(
            @RequestHeader(Header.userIdHeader) @Min(1) Long ownerId,
            @RequestParam String approved,
            @PathVariable @Min(1) Long bookingId) {
        return bookingClient.approveBooking(ownerId, approved, bookingId);
    }

    @GetMapping("{bookingId}")
    public Mono<ResponseEntity<Object>> getBookingByIdForOwnerAndBooker(
            @PathVariable @Min(1) Long bookingId,
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId) {
        return bookingClient.getBookingByIdForOwnerAndBooker(bookingId, userId);
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> getAllBookingsForUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return bookingClient.getAllBookingsForUser(userId, state, from, size);
    }

    @GetMapping("owner")
    public Mono<ResponseEntity<Object>> getAllBookingsForItemsUser(
            @RequestHeader(Header.userIdHeader) @Min(1) Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") @Min(0) Integer from,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(20) Integer size) {
        return bookingClient.getAllBookingsForItemsUser(userId, state, from, size);
    }
}