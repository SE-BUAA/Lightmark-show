package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.timemark.backend.dto.module.TrainCalendarRequest;
import top.ortus.timemark.backend.dto.module.TrainSearchRequest;
import top.ortus.timemark.backend.dto.module.TrainStationOptionsResponse;
import top.ortus.timemark.backend.service.TrainService;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainController {
    @Autowired
    private TrainService trainService;

    @GetMapping("/options")
    public ApiResponse<TrainStationOptionsResponse> stationOptions() {
        return ApiResponse.ok(trainService.stationOptions());
    }

    @PostMapping("/search")
    public ApiResponse<List<Product>> search(@RequestBody(required = false) TrainSearchRequest request) {
        return ApiResponse.ok(trainService.search(request));
    }

    @PostMapping("/calendar")
    public ApiResponse<List<TrainCalendarDayResponse>> calendar(@RequestBody(required = false) TrainCalendarRequest request) {
        return ApiResponse.ok(trainService.calendar(request));
    }

    @GetMapping("/calendar")
    public ApiResponse<List<TrainCalendarDayResponse>> calendarByQuery(TrainCalendarRequest request) {
        return ApiResponse.ok(trainService.calendar(request));
    }

    @GetMapping("/detail/{productId}")
    public ApiResponse<Product> searchById(@PathVariable("productId") Integer productId) {
        return ApiResponse.ok(trainService.searchById(productId));
    }
}
