package top.ortus.lightmark.backend.dto.module;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationAssistantResponse {
    private String orderNo;
    private String destination;
    private String date;
    private String content;
}
