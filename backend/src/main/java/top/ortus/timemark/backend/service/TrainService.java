package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.timemark.backend.dto.module.TrainCalendarRequest;
import top.ortus.timemark.backend.dto.module.TrainStationOptionsResponse;

import java.util.List;

public interface TrainService {
    List<Product> search(top.ortus.timemark.backend.dto.module.TrainSearchRequest request);

    List<TrainCalendarDayResponse> calendar(TrainCalendarRequest request);

    Product searchById(Integer productId);

    TrainStationOptionsResponse stationOptions();
}
