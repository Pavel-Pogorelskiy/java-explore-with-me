package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.*;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.model.SortComment;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class PublicController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final CompilationService compilationService;
    private final StatsClient statsClient;

    @GetMapping(value = "/categories/{catId}")
    CategoryDto getCategory(@PathVariable @Positive int catId) {
        log.info("Get category by id ={}", catId);
        return categoryService.getCategory(catId);
    }

    @GetMapping(value = "/categories")
    public List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @Min(0) int from,
                                           @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get all categories from = {}, size = {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping(value = "/events/{id}")
    public EventFullDto getEvent(@PathVariable @Positive int id, HttpServletRequest request) {
        log.info("Get event by id = {}", id);
        statsClient.saveHit(new EndpointHit("ewm-main-service",
                request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return eventService.getEvent(id);
    }

    @GetMapping(value = "/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable int compId) {
        log.info("Get compilation for id = {}", compId);
        return compilationService.getCompilation(compId);
    }

    @GetMapping(value = "/compilations")
    public List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                                @RequestParam(defaultValue = "0") int from,
                                                @RequestParam(defaultValue = "10") int size) {
        log.info("Get compilations for pinned = {}, from = {}, size = {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping(value = "/events")
    public List<EventShortDto> getEvents(@RequestParam(required = false) String text,
                                  @RequestParam(required = false) List<Integer> categories,
                                  @RequestParam(required = false) Boolean paid,
                                  @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                  @RequestParam(defaultValue = "false") String sort,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                 LocalDateTime rangeStart,
                                  @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                 LocalDateTime rangeEnd,
                                  @RequestParam(defaultValue = "0") @Min(0) int from,
                                  @RequestParam(defaultValue = "10") @Min(1) int size,
                                  HttpServletRequest request) {
        log.info("Get events text = {}, categories = {}, paid = {}, onlyAvailable = {}," +
                "sort = {}, rangeStart = {}, rangeEnd = {}, from = {} " +
                "size = {}", text, categories, paid, onlyAvailable, sort, rangeStart, rangeEnd, from, size);
        statsClient.saveHit(new EndpointHit("ewm-main-service",
                request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return eventService.getEventsByFilter(text, categories, paid, rangeStart, rangeEnd, onlyAvailable,
                sort, from, size);
    }

    @GetMapping(value = "/events/{eventId}/comments")
    public List<CommentShortDto> getComments(@PathVariable @Positive int eventId,
                                             @RequestParam(defaultValue = "0") @Min(0) int from,
                                             @RequestParam(defaultValue = "10") @Min(1) int size,
                                             @RequestParam(defaultValue = "DESCCREATEDDATA") SortComment sortComment) {
        log.info("Get a comments eventId = " + eventId + "from = " + from
        + "size = " + size + "sort" + sortComment);
        return eventService.getCommentToEvent(eventId, from, size, sortComment);
    }
}
