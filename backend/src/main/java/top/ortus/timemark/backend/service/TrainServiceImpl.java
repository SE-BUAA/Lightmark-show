package top.ortus.timemark.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;
import top.ortus.timemark.backend.dto.module.TrainCalendarDayResponse;
import top.ortus.timemark.backend.dto.module.TrainCalendarRequest;
import top.ortus.timemark.backend.dto.module.TrainSearchRequest;
import top.ortus.timemark.backend.dto.module.TrainStationOptionsResponse;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class TrainServiceImpl implements TrainService {
    private static final List<String> TRAIN_STATIONS = List.of(
        "\u5317\u4eac\u5357",
        "\u4e0a\u6d77\u8679\u6865",
        "\u5e7f\u5dde\u5357",
        "\u6df1\u5733\u5317",
        "\u676d\u5dde\u4e1c",
        "\u5357\u4eac\u5357",
        "\u6b66\u6c49",
        "\u897f\u5b89\u5317",
        "\u6210\u90fd\u4e1c",
        "\u91cd\u5e86\u5317",
        "\u90d1\u5dde\u4e1c",
        "\u957f\u6c99\u5357",
        "\u5929\u6d25\u897f",
        "\u9752\u5c9b\u5317",
        "\u53a6\u95e8\u5317",
        "\u6606\u660e\u5357",
        "\u5357\u660c\u897f",
        "\u5408\u80a5\u5357",
        "\u82cf\u5dde\u5317",
        "\u6d4e\u5357\u897f"
    );

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> search(TrainSearchRequest request) {
        LambdaQueryWrapper<Product> queryWrapper = activeTrainQuery();
        if (request != null) {
            applyExtraEquals(queryWrapper, "start_station", request.getStartStation());
            applyExtraEquals(queryWrapper, "end_station", request.getEndStation());
            applyExtraEquals(queryWrapper, "date", request.getDate());
            applyAnyTag(queryWrapper, request.getTrainTypes());
            applyAnyTag(queryWrapper, request.getSeatTypes());
        }
        queryWrapper.orderByAsc(Product::getId);
        if (request == null || isBlank(request.getDate())) {
            queryWrapper.last("LIMIT 500");
        }
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public List<TrainCalendarDayResponse> calendar(TrainCalendarRequest request) {
        LambdaQueryWrapper<Product> queryWrapper = activeTrainQuery();
        if (request != null) {
            applyExtraEquals(queryWrapper, "start_station", request.getStartStation());
            applyExtraEquals(queryWrapper, "end_station", request.getEndStation());
            applyExtraMonth(queryWrapper, request.getMonth());
            applyAnyTag(queryWrapper, request.getTrainTypes());
            applyAnyTag(queryWrapper, request.getSeatTypes());
        }
        queryWrapper.select(Product::getStock, Product::getSoldCount, Product::getExtra);
        List<Product> trains = productMapper.selectList(queryWrapper);
        Map<String, List<Product>> byDate = trains.stream()
            .filter(product -> product.getExtra() != null && product.getExtra().get("date") != null)
            .collect(Collectors.groupingBy(product -> String.valueOf(product.getExtra().get("date"))));
        return byDate.entrySet().stream()
            .map(entry -> new TrainCalendarDayResponse(
                entry.getKey(),
                entry.getValue().stream().mapToInt(this::availableTickets).sum(),
                (int) entry.getValue().stream().filter(product -> availableTickets(product) > 0).count()
            ))
            .sorted(Comparator.comparing(TrainCalendarDayResponse::getDate))
            .toList();
    }

    @Override
    public Product searchById(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public TrainStationOptionsResponse stationOptions() {
        return new TrainStationOptionsResponse(
            TRAIN_STATIONS,
            TRAIN_STATIONS,
            defaultDateOptions()
        );
    }

    private LambdaQueryWrapper<Product> activeTrainQuery() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, "TRAIN").eq(Product::getStatus, 1);
        return queryWrapper;
    }

    private void applyExtraEquals(LambdaQueryWrapper<Product> queryWrapper, String key, String expected) {
        if (!isBlank(expected)) {
            queryWrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(extra, '$." + key + "')) = {0}", expected);
        }
    }

    private void applyExtraMonth(LambdaQueryWrapper<Product> queryWrapper, String month) {
        if (!isBlank(month)) {
            queryWrapper.apply("JSON_UNQUOTE(JSON_EXTRACT(extra, '$.date')) LIKE {0}", month + "%");
        }
    }

    private void applyAnyTag(LambdaQueryWrapper<Product> queryWrapper, List<String> expectedTags) {
        if (expectedTags == null || expectedTags.isEmpty()) {
            return;
        }
        queryWrapper.and(wrapper -> {
            for (int i = 0; i < expectedTags.size(); i++) {
                String tag = expectedTags.get(i);
                if (i == 0) {
                    wrapper.apply("JSON_CONTAINS(category_tags, JSON_QUOTE({0}))", tag);
                } else {
                    wrapper.or().apply("JSON_CONTAINS(category_tags, JSON_QUOTE({0}))", tag);
                }
            }
        });
    }

    private int availableTickets(Product product) {
        return Math.max(0, Objects.requireNonNullElse(product.getStock(), 0) - Objects.requireNonNullElse(product.getSoldCount(), 0));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private List<String> defaultDateOptions() {
        LocalDate start = LocalDate.of(2026, 6, 1);
        return IntStream.range(0, 180)
            .mapToObj(offset -> start.plusDays(offset).toString())
            .toList();
    }
}
