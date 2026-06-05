package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.timemark.backend.dto.module.TrainCalendarRequest;
import top.ortus.timemark.backend.dto.module.TrainStationOptionsResponse;
import top.ortus.timemark.backend.dto.module.TrainTicketDTO;

import java.util.List;

public interface TrainService {
    List<TrainTicketDTO> search(top.ortus.timemark.backend.dto.module.TrainSearchRequest request);

    List<TrainTicketDTO> searchTransfers(top.ortus.timemark.backend.dto.module.TrainSearchRequest request);

    List<TrainCalendarDayResponse> calendar(TrainCalendarRequest request);

    TrainTicketDTO searchById(String ticketId);

    TrainStationOptionsResponse stationOptions();
}
