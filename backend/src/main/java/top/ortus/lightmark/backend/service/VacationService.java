package top.ortus.lightmark.backend.service;

import top.ortus.lightmark.backend.dao.Product;
import top.ortus.lightmark.backend.dto.module.VacationAiDetailResponse;
import top.ortus.lightmark.backend.dto.module.VacationOptionsResponse;
import top.ortus.lightmark.backend.dto.module.VacationSearchRequest;

import java.util.List;

public interface VacationService {
    List<Product> search(VacationSearchRequest request);

    Product searchById(Integer productId);

    VacationOptionsResponse options();

    VacationAiDetailResponse generateAiDetail(Integer productId);
}
