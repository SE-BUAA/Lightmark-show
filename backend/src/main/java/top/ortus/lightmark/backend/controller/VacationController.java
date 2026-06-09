package top.ortus.lightmark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.dao.Product;
import top.ortus.lightmark.backend.dto.module.VacationAiDetailResponse;
import top.ortus.lightmark.backend.dto.module.VacationOptionsResponse;
import top.ortus.lightmark.backend.dto.module.VacationSearchRequest;
import top.ortus.lightmark.backend.service.VacationService;

import java.util.List;

@RestController
@RequestMapping("/api/vacations")
public class VacationController {
    @Autowired
    private VacationService vacationService;

    @GetMapping("/options")
    public ApiResponse<VacationOptionsResponse> options() {
        return ApiResponse.ok(vacationService.options());
    }

    @GetMapping("/search")
    public ApiResponse<List<Product>> search() {
        List<Product> vacations = vacationService.search(null);
        return ApiResponse.ok(vacations);
    }

    @PostMapping("/search")
    public ApiResponse<List<Product>> search(@RequestBody(required = false) VacationSearchRequest request) {
        List<Product> vacations = vacationService.search(request);
        return ApiResponse.ok(vacations);
    }
    @GetMapping("/{productId}")
    public ApiResponse<Product> searchById(@PathVariable("productId") Integer productId) {
        return ApiResponse.ok(vacationService.searchById(productId));
    }

    @GetMapping("/{productId}/detail-ai")
    public ApiResponse<VacationAiDetailResponse> generateAiDetail(@PathVariable("productId") Integer productId) {
        return ApiResponse.ok(vacationService.generateAiDetail(productId));
    }
}
