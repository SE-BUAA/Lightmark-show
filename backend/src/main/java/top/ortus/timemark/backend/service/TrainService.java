package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dao.Product;

import java.util.List;

public interface TrainService {
    List<Product> search();

    Product searchById(Integer productId);
}
