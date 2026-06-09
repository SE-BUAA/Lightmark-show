package top.ortus.lightmark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationOptionsResponse {
    private List<String> destinations;
    private List<String> departCities;
    private List<String> dates;
    private List<String> tags;
}
