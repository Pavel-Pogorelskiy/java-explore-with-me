package ru.practicum.ewm.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.model.Status;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@NoArgsConstructor
public class EventRequestStatusUpdateRequest {
    List<Integer> requestIds;
    @NotNull
    Status status;
}
