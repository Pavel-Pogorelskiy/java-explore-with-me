package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.mapper.EventAndCompilationMapper;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.JpaCategoriesRepository;
import ru.practicum.ewm.repository.JpaEventsRepository;
import ru.practicum.ewm.repository.JpaUsersRepository;
import ru.practicum.ewm.utils.OffsetBasedPageRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final JpaEventsRepository repositoryEvent;
    private final JpaCategoriesRepository repositoryCategory;
    private final JpaUsersRepository repositoryUser;
    private final EventAndCompilationMapper mapper;
    private final StatsClient statsClient;

    @Override
    @Transactional
    public EventFullDto saveEvent(int userId, NewEventDto newEventDto) {
        User user = repositoryUser.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id =" + userId + " was not found"));
        Event event = mapper.toModelEvent(newEventDto);
        Category category = repositoryCategory.findById(event.getCategory().getId())
                .orElseThrow(() -> new NotFoundException("Category with id =" + event.getCategory().getId()
                        + " was not found"));
        if (LocalDateTime.now().plusHours(2).isAfter(event.getEventDate())) {
            throw new BadRequestException("Event date cannot be earlier than two hours later");
        }
        event.setInitiator(user);
        event.setCategory(category);
        return mapper.toFullDtoEvent(repositoryEvent.save(event));
    }

    @Override
    @Transactional
    public EventFullDto updateEventUser(int userId, int eventId, UpdateEventUserRequest updateEventUserRequest) {
        Event event = repositoryEvent.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + "for initiator = " + userId
                        + " was not found"));
        if (updateEventUserRequest.getEventDate() != null &&
                updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date cannot be earlier than two hours later");
        }
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictRequestException("The event has already been published");
        }
        return mapper.toFullDtoEvent(repositoryEvent.save(changeEventByUser(event, updateEventUserRequest)));
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(int eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = repositoryEvent.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " was not found"));
        return mapper.toFullDtoEvent(repositoryEvent.save(changeEventByAdmin(event, updateEventAdminRequest)));
    }

    @Override
    public EventFullDto getEventToUser(int userId, int eventId) {
        repositoryUser.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id =" + userId + " was not found"));
        Event event = repositoryEvent.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id =" + eventId + " was not found"));
        if (event.getState() == State.PUBLISHED) {
            List<ViewStats> stats = statsClient.getStats(event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    List.of("/events/" + eventId), true);
            if (!stats.isEmpty()) {
                event.setViews(stats.get(0).getHits());
            }
        }
        return mapper.toFullDtoEvent(event);
    }

    @Override
    public EventFullDto getEvent(int eventId) {
        Event event = repositoryEvent.findByIdAndState(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id =" + eventId
                        + " and PUBLISHED was not found"));
        List<ViewStats> stats = statsClient.getStats(event.getPublishedOn().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                List.of("/events/" + eventId), true);
        if (!stats.isEmpty()) {
            event.setViews(stats.get(0).getHits());
        }
        return mapper.toFullDtoEvent(event);
    }

    @Override
    public List<EventShortDto> getShortEvents(int userId, int from, int size) {
        repositoryUser.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id =" + userId + " was not found"));
        List<Event> events = repositoryEvent.findByInitiatorId(userId, new OffsetBasedPageRequest(from, size));
        List<String> uri = getUris(events);
        if (!uri.isEmpty()) {
            events = getViews(uri, events);
        }
        return mapper.toDtoShortEvents(events);
    }

    @Override
    public List<EventFullDto> getFullEvents(List<Integer> users, List<State> states, List<Integer> categories,
                                            LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        if (states == null) {
            states = List.of(State.PENDING, State.CANCELED, State.PUBLISHED);
        }
        if (rangeStart == null) {
            rangeStart = LocalDateTime.of(0, 1, 1, 0, 0, 0);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10000);
        }
        List<Event> events;
        if (users == null && categories == null) {
            events = repositoryEvent.findByStateInAndEventDateAfterAndEventDateBefore(
                    states, rangeStart, rangeEnd, new OffsetBasedPageRequest(from, size));
        } else if (users == null) {
            events = repositoryEvent.findByStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(states,
                    categories, rangeStart, rangeEnd, new OffsetBasedPageRequest(from, size));
        } else if (categories == null) {
            events = repositoryEvent.findByInitiatorIdInAndStateInAndEventDateAfterAndEventDateBefore(users,
                    states, rangeStart, rangeEnd, new OffsetBasedPageRequest(from, size));
        } else {
            events = repositoryEvent
                    .findByInitiatorIdInAndStateInAndCategoryIdInAndEventDateAfterAndEventDateBefore(users, states,
                            categories, rangeStart, rangeEnd, new OffsetBasedPageRequest(from, size));
        }
        List<String> uri = getUris(events);
        if (!uri.isEmpty()) {
            events = getViews(uri, events);
        }
        return mapper.toDtoFullEvents(events);
    }

    @Override
    public List<EventShortDto> getEventsByFilter(String text, List<Integer> categories, Boolean paid,
                                                 LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                 Boolean onlyAvailable, String sort, int from, int size) {
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now();
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(10000);
        }
        if (!rangeEnd.isAfter(rangeStart)) {
            throw new BadRequestException("Range end must be after range start");
        }
        List<Event> events;
        if (paid == null) {
            if (text == null && categories == null) {
                events = repositoryEvent.findByStatePublishedAndEventDateAfterAndEventDateBefore(rangeStart,
                        rangeEnd);
            } else if (text == null) {
                events = repositoryEvent.findByStatePublishedAndCategoryIdInAndEventDateAfterAndEventDateBefore(categories,
                        rangeStart, rangeEnd);
            } else if (categories == null) {
                events = repositoryEvent.findByStatePublishedAndTextAndEventDateAfterAndEventDateBefore(text,
                        rangeStart, rangeEnd);
            } else {
                events = repositoryEvent.findByStatePublishedFullFilter(text, categories,
                        rangeStart, rangeEnd);
            }
        } else {
            if (text.isEmpty() && categories == null) {
                events = repositoryEvent.findByStatePublishedAndEventDateAfterAndEventDateBefore(rangeStart,
                        rangeEnd, paid);
            } else if (text.isEmpty()) {
                events = repositoryEvent.findByStatePublishedAndCategoryIdInAndEventDateAfterAndEventDateBefore(
                        categories, rangeStart, rangeEnd, paid);
            } else if (categories == null) {
                events = repositoryEvent.findByStatePublishedAndTextAndEventDateAfterAndEventDateBefore(text,
                        rangeStart, rangeEnd, paid);
            } else {
                events = repositoryEvent.findByStatePublishedFullFilter(text, categories,
                        rangeStart, rangeEnd, paid);
            }
        }
        if (events.isEmpty()) {
            new ArrayList<>();
        }
        if (onlyAvailable) {
            events = events.stream()
                    .filter(event -> event.getParticipantLimit() >= event.getRequests().stream().filter(r ->
                            r.getStatus() == Status.CONFIRMED).count())
                    .collect(Collectors.toList());
        }
        List<String> uri = getUris(events);
        if (!uri.isEmpty()) {
            events = getViews(uri, events);
        }
        if (sort != null) {
            switch (sort) {
                case "VIEWS":
                    events = events.stream()
                            .sorted(Comparator.comparingInt(Event::getViews))
                            .collect(Collectors.toList());
                    break;
                case "EVENT_DATE":
                    events = events.stream()
                            .sorted(this::compareEventForDate)
                            .collect(Collectors.toList());
                    break;
            }
        }
        return mapper.toDtoShortEvents(events).stream()
                .skip(from)
                .limit(size)
                .collect(Collectors.toList());
    }

    private int compareEventForDate(Event a, Event b) {
        return a.getEventDate().compareTo(b.getEventDate());
    }

    private Event changeEventByUser(Event event, UpdateEventUserRequest newEvent) {
        changeEvent(event, newEvent);
        if (newEvent.getStateAction() != null) {
            switch (newEvent.getStateAction()) {
                case CANCEL_REVIEW:
                    event.setState(State.CANCELED);
                    break;
                case SEND_TO_REVIEW:
                    event.setState(State.PENDING);
                    break;
            }
        }
        return event;
    }

    private Event changeEventByAdmin(Event event, UpdateEventAdminRequest newEvent) {
        changeEvent(event, newEvent);
        if (event.getState() == State.PUBLISHED) {
            if (event.getEventDate().isBefore(event.getPublishedOn().plusHours(1))) {
                throw new ConflictRequestException("Event date should be no earlier than one hour after publication");
            }
        }
        if (newEvent.getStateAction() != null) {

            switch (newEvent.getStateAction()) {
                case PUBLISH_EVENT:
                    if (event.getState() != State.PENDING) {
                        throw new ConflictRequestException("Event should be in pending state");
                    }
                    LocalDateTime now = LocalDateTime.now();
                    if (event.getEventDate().isBefore(now.plusHours(1))) {
                        throw new ConflictRequestException("Event date should be no earlier than one hour after publication");
                    }
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(now);
                    break;
                case REJECT_EVENT:
                    if (event.getState() == State.PUBLISHED) {
                        throw new ConflictRequestException("Event should be in not published state");
                    }
                    event.setState(State.CANCELED);
                    break;
            }
        }
        return event;
    }

    private <T extends BaseUpdateEventRequest> void changeEvent(Event event, T newEvent) {
        if (newEvent.getTitle() != null) {
            event.setTitle(newEvent.getTitle());
        }
        if (newEvent.getAnnotation() != null) {
            event.setAnnotation(newEvent.getAnnotation());
        }
        if (newEvent.getDescription() != null) {
            event.setDescription(newEvent.getDescription());
        }
        if (newEvent.getEventDate() != null) {
            if (!newEvent.getEventDate().isAfter(LocalDateTime.now())) {
                throw new BadRequestException("Event date must be in future");
            }
            event.setEventDate(newEvent.getEventDate());
        }
        if (newEvent.getLocation() != null) {
            event.getLocation().setLat(newEvent.getLocation().getLat());
            event.getLocation().setLon(newEvent.getLocation().getLon());
        }
        if (newEvent.getParticipantLimit() != null) {
            event.setParticipantLimit(newEvent.getParticipantLimit());
        }
        if (newEvent.getPaid() != null) {
            event.setPaid(newEvent.getPaid());
        }
        if (newEvent.getRequestModeration() != null) {
            event.setRequestModeration(newEvent.getRequestModeration());
        }
        if (newEvent.getCategory() != null) {
            Category category = repositoryCategory.findById(newEvent.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id = " +
                            newEvent.getCategory() + "not found"));
            event.setCategory(category);
        }
    }

    private List<Event> getViews(List<String> uri, List<Event> events) {
        Map<String, Integer> views = statsClient.getStats(LocalDateTime.now().minusYears(1000)
                                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        uri, true).stream()
                .collect(Collectors.toMap(ViewStats::getUri, ViewStats::getHits));
        List<Event> results = new ArrayList<>();
        for (Event event : events) {
            event.setViews(views.getOrDefault("/events/" + event.getId(), 0));
            results.add(event);
        }
        return results;
    }

    private List<String> getUris(List<Event> events) {
        return events.stream()
                .filter(i -> i.getState() == State.PUBLISHED)
                .sorted((o1, o2) -> o2.getPublishedOn().compareTo(o1.getPublishedOn()))
                .map(i -> "/events/" + i.getId())
                .collect(Collectors.toList());
    }
}