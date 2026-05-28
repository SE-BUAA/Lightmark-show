package top.ortus.timemark.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;
import top.ortus.timemark.backend.dto.module.TrainSearchRequest;
import top.ortus.timemark.backend.dto.module.TrainStationOptionsResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class TrainServiceImpl implements TrainService {
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> search(TrainSearchRequest request) {
        List<Product> trains = selectActiveTrains();
        if (request == null) {
            return trains;
        }

        return trains.stream()
            .filter(product -> matchesExtra(product, "start_station", request.getStartStation()))
            .filter(product -> matchesExtra(product, "end_station", request.getEndStation()))
            .filter(product -> matchesExtra(product, "date", request.getDate()))
            .filter(product -> matchesAnyTag(product, request.getTrainTypes()))
            .filter(product -> matchesAnyTag(product, request.getSeatTypes()))
            .toList();
    }

    @Override
    public Product searchById(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public TrainStationOptionsResponse stationOptions() {
        List<Product> trains = selectActiveTrains();
        return new TrainStationOptionsResponse(
            distinctExtraValues(trains, "start_station"),
            distinctExtraValues(trains, "end_station"),
            distinctExtraValues(trains, "date")
        );
    }

    private List<Product> selectActiveTrains() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, "TRAIN")
            .eq(Product::getStatus, 1)
            .orderByAsc(Product::getId);
        return productMapper.selectList(queryWrapper);
    }

    private boolean matchesExtra(Product product, String key, String expected) {
        if (expected == null || expected.isBlank()) {
            return true;
        }
        Object actual = product.getExtra() == null ? null : product.getExtra().get(key);
        return expected.equals(String.valueOf(actual));
    }

    private boolean matchesAnyTag(Product product, List<String> expectedTags) {
        if (expectedTags == null || expectedTags.isEmpty()) {
            return true;
        }
        List<String> tags = product.getCategoryTags();
        if (tags == null || tags.isEmpty()) {
            return false;
        }
        return expectedTags.stream().anyMatch(tags::contains);
    }

    private List<String> distinctExtraValues(List<Product> products, String key) {
        return products.stream()
            .map(product -> product.getExtra() == null ? null : product.getExtra().get(key))
            .filter(Objects::nonNull)
            .map(String::valueOf)
            .filter(value -> !value.isBlank())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();
    }
}
