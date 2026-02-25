package ru.yandex.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.shareit.booking.dto.BookingCreateDto;
import ru.yandex.practicum.shareit.booking.dto.BookingDto;
import ru.yandex.practicum.shareit.booking.model.BookingStatus;
import ru.yandex.practicum.shareit.booking.service.BookingService;
import ru.yandex.practicum.shareit.controller.BookingController;
import ru.yandex.practicum.shareit.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto bookingDto;
    private BookingCreateDto bookingCreateDto;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        bookingDto = BookingDto.builder()
                .id(1L)
                .start(now.plusDays(1))
                .end(now.plusDays(2))
                .status(BookingStatus.WAITING)
                .build();

        bookingCreateDto = new BookingCreateDto();
        bookingCreateDto.setItemId(1L);
        bookingCreateDto.setStart(now.plusDays(1));
        bookingCreateDto.setEnd(now.plusDays(2));
    }

    @Test
    void getUserBookings_ShouldReturnList() throws Exception {
        when(bookingService.getUserBookings(eq(1L), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getOwnerBookings_ShouldReturnList() throws Exception {
        when(bookingService.getOwnerBookings(eq(1L), eq("ALL"), anyInt(), anyInt()))
                .thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getBookingById_WithValidId_ShouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(1L, 1L)).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getBookingById_WithInvalidId_ShouldReturn404() throws Exception {
        when(bookingService.getBookingById(1L, 99L))
                .thenThrow(new NotFoundException("Бронирование с id 99 не найдено"));

        mockMvc.perform(get("/bookings/99")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_WithValidData_ShouldReturnBooking() throws Exception {
        when(bookingService.createBooking(eq(2L), eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingCreateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveBooking_WithValidData_ShouldReturnApprovedBooking() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true)).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveBooking_ByNonOwner_ShouldReturn404() throws Exception {
        when(bookingService.approveBooking(2L, 1L, true))
                .thenThrow(new NotFoundException("Подтверждение бронирования доступно только владельцу вещи"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 2L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }
}