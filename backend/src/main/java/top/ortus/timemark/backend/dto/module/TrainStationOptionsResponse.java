package top.ortus.timemark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainStationOptionsResponse {
    private List<String> startStations;
    private List<String> endStations;
    private List<String> dates;
}
