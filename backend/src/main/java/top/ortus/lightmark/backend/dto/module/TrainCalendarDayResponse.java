package top.ortus.lightmark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainCalendarDayResponse {
    private String date;
    private Integer ticketCount;
    private Integer trainCount;
}
