package top.ortus.lightmark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.lightmark.backend.dto.module.TrainCalendarRequest;
import top.ortus.lightmark.backend.dto.module.TrainSearchRequest;
import top.ortus.lightmark.backend.dto.module.TrainStationOptionsResponse;
import top.ortus.lightmark.backend.dto.module.TrainTicketDTO;
import top.ortus.lightmark.backend.service.TrainService;

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
    public ApiResponse<List<TrainTicketDTO>> search(@RequestBody(required = false) TrainSearchRequest request) {
        return ApiResponse.ok(trainService.search(request));
    }

    @PostMapping("/transfer/search")
    public ApiResponse<List<TrainTicketDTO>> searchTransfers(@RequestBody(required = false) TrainSearchRequest request) {
        return ApiResponse.ok(trainService.searchTransfers(request));
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
    public ApiResponse<TrainTicketDTO> searchById(@PathVariable("productId") String productId) {
        return ApiResponse.ok(trainService.searchById(productId));
    }
}
