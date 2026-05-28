package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dto.module.VacationOptionsResponse;
import top.ortus.timemark.backend.dto.module.VacationSearchRequest;

import java.util.List;

public interface VacationService {
    List<Product> search(VacationSearchRequest request);

    Product searchById(Integer productId);

    VacationOptionsResponse options();
}
