package top.ortus.timemark.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.service.TrainService;

import java.util.List;

@RestController
@RequestMapping("/trains")
public class TrainController {
    @Autowired
    private TrainService trainService;

    @GetMapping("/search")
    public ApiResponse<List<Product>> search() {
        List<Product> trains = trainService.search();
        return ApiResponse.ok(trains);
    }
    @GetMapping("/{productId}")
    public ApiResponse<Product> searchById(@PathVariable("productId") Integer productId) {
        return ApiResponse.ok(trainService.searchById(productId));
    }
}
