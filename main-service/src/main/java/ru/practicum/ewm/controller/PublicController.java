package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.client.stats.StatsClient;
import ru.practicum.ewm.dto.CategoryDto;
import ru.practicum.ewm.dto.CompilationDto;
import ru.practicum.ewm.dto.EventFullDto;
import ru.practicum.ewm.dto.EventShortDto;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.service.EventService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;
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
    CategoryDto getCategory(@PathVariable int catId) {
        log.info("Get category by id ={}", catId);
        return categoryService.getCategory(catId);
    }

    @GetMapping(value = "/categories")
    List<CategoryDto> getCategories(@RequestParam(defaultValue = "0") @Min(0) int from,
                                    @RequestParam(defaultValue = "10") @Min(1) int size) {
        log.info("Get all categories from = {}, size = {}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping(value = "/events/{id}")
    EventFullDto getEvent(@PathVariable int id, HttpServletRequest request) {
        log.info("Get event by id = {}", id);
        statsClient.saveHit(new EndpointHit("ewm-main-service",
                request.getRequestURI(), request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        return eventService.getEvent(id);
    }

    @GetMapping(value = "/compilations/{compId}")
    CompilationDto getCompilation(@PathVariable int compId) {
        log.info("Get compilation for id = {}", compId);
        return compilationService.getCompilation(compId);
    }

    @GetMapping(value = "/compilations")
    List<CompilationDto> getCompilations(@RequestParam(required = false) Boolean pinned,
                                         @RequestParam(defaultValue = "0") int from,
                                         @RequestParam(defaultValue = "10") int size) {
        log.info("Get compilations for pinned = {}, from = {}, size = {}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping(value = "/events")
    List<EventShortDto> getEvents(@RequestParam(required = false) String text,
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
}
