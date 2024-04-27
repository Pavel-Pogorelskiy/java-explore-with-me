package ru.practicum.ewm.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.stats.service.StatsService;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StatsController.class)
class StatsControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private StatsService service;
    @Autowired
    private MockMvc mvc;

    @Test
    void saveHitTestExceptionValidateApi() throws Exception {
        EndpointHit hit = new EndpointHit("", "/events", "100.111.11.11", "2022-09-06 11:00:23");

        mvc.perform(post("/hit")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(hit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveHitTestExceptionValidateUri() throws Exception {
        EndpointHit hit = new EndpointHit("ewm", "", "100.111.11.11", "2022-09-06 11:00:23");

        mvc.perform(post("/hit")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(hit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveHitTestExceptionValidateIp() throws Exception {
        EndpointHit hit = new EndpointHit("ewm", "/event", "", "2022-09-06 11:00:23");

        mvc.perform(post("/hit")
                        .header("X-Sharer-User-Id", 1)
                        .content(mapper.writeValueAsString(hit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveHitTestExceptionValidateTimeStamp() throws Exception {
        EndpointHit hit = new EndpointHit("ewm", "/event", "", "");

        mvc.perform(post("/hit")
                        .content(mapper.writeValueAsString(hit))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatsTestException() throws Exception {
        mvc.perform(get("/stats?start=00:00:00 2025-05-05&end=00:00:00 2035-05-05&unique=false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

}