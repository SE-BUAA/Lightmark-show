package top.ortus.lightmark.backend.service;

import top.ortus.lightmark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.lightmark.backend.dto.module.TrainSearchRequest;
import top.ortus.lightmark.backend.dto.module.TrainCalendarRequest;
import top.ortus.lightmark.backend.dto.module.TrainStationOptionsResponse;
import top.ortus.lightmark.backend.dto.module.TrainTicketDTO;

import java.util.List;

public interface TrainService {
    List<TrainTicketDTO> search(TrainSearchRequest request);

    List<TrainTicketDTO> searchTransfers(TrainSearchRequest request);

    List<TrainCalendarDayResponse> calendar(TrainCalendarRequest request);

    TrainTicketDTO searchById(String ticketId);

    TrainStationOptionsResponse stationOptions();
}
