package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.dto.stats.ViewStats;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.Stats;
import ru.practicum.ewm.stats.repository.JpaStatsRepository;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final JpaStatsRepository repository;
    private final StatsMapper mapper;

    @Override
    @Transactional
    public void save(EndpointHit endpointHit) {
        Stats stats = mapper.toModelStats(endpointHit);
        if (stats.getTimestamp().isAfter(LocalDateTime.now())) {
            throw new ValidationException("Timestamp after to LocalDateTime.now");
        }
        repository.save(stats);
    }

    @Override
    public List<ViewStats> get(LocalDateTime start, LocalDateTime end, List<String> uris, boolean isUnique) {
        List<ViewStats> stats;
        if (!isUnique) {
            if (uris == null) {
                stats = mapper.toDtoStats(repository.findByNotUri(start, end));
            } else {
                stats = mapper.toDtoStats(repository.findByUri(start, end, uris));
            }
        } else {
            if (uris == null) {
                stats = mapper.toDtoStats(repository.findByNotUriDistinct(start, end));
            } else {
                stats = mapper.toDtoStats(repository.findByUriDistinct(start, end, uris));
            }
        }
        return stats;
    }
}
