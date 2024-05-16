package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class PrivateController {
    private final EventService eventService;
    private final RequestService requestService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{userId}/events")
    EventFullDto saveEvent(@PathVariable int userId,
                           @RequestBody @Valid NewEventDto eventDto) {
        log.info("Created new event {}, userId = {}", eventDto, userId);
        return eventService.saveEvent(userId, eventDto);
    }

    @GetMapping(value = "/{userId}/events/{eventId}")
    EventFullDto getEvent(@PathVariable int userId, @PathVariable int eventId) {
        log.info("Get event id= {}, userId = {}", eventId, userId);
        return eventService.getEventToUser(userId, eventId);
    }

    @GetMapping(value = "/{userId}/events")
    List<EventShortDto> getEvents(@PathVariable int userId,
                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                  @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get events userId = {}, from = {}, size = {}", userId, from, size);
        return eventService.getShortEvents(userId, from, size);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}")
    EventFullDto updateEvent(@PathVariable int userId,
                             @PathVariable int eventId,
                             @RequestBody @Valid UpdateEventUserRequest updateEventUserRequest) {
        log.info("Update event eventId = {}, userId = {}, request = {}", eventId, userId, updateEventUserRequest);
        return eventService.updateEventUser(userId, eventId, updateEventUserRequest);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/{userId}/requests")
    ParticipationRequestDto saveRequest(@PathVariable int userId,
                                      @RequestParam int eventId) {
        log.info("Created new request eventId = {}, userId = {}", eventId, userId);
        return requestService.saveRequest(userId, eventId);
    }

    @PatchMapping(value = "/{userId}/requests/{requestId}/cancel")
    ParticipationRequestDto cancelRequest(@PathVariable int userId,
                                      @PathVariable int requestId) {
        log.info("Cancel request requestId = {}, userId = {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping(value = "/{userId}/requests")
    List<ParticipationRequestDto> getRequests(@PathVariable int userId) {
        log.info("Get requests userId = {}",userId);
        return requestService.getRequestsToUser(userId);
    }

    @PatchMapping(value = "/{userId}/events/{eventId}/requests")
    EventRequestStatusUpdateResult confirmedOrRejectedRequests(@PathVariable int userId,
                                                        @PathVariable int eventId,
                                                        @RequestBody @Valid EventRequestStatusUpdateRequest request) {
        log.info("Confirmed/Rejected request userID = {}, eventId = {}, request = {}", userId, eventId, request);
        return requestService.confirmedOrRejectedRequests(userId, eventId, request);
    }

    @GetMapping(value = "/{userId}/events/{eventId}/requests")
    List<ParticipationRequestDto> getRequestsToEvent(@PathVariable int userId,
                                                           @PathVariable int eventId) {
        log.info("Get requests to userId = {}, eventId = {}",userId, eventId);
        return requestService.getRequestsToEvent(userId, eventId);
    }
}
