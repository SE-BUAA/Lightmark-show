package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;
import top.ortus.timemark.backend.dto.module.VacationOptionsResponse;
import top.ortus.timemark.backend.dto.module.VacationSearchRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class VacationServiceImpl implements VacationService{
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> search(VacationSearchRequest request) {
        List<Product> vacations = selectActiveVacations();
        if (request == null) {
            return vacations;
        }
        return vacations.stream()
            .filter(product -> matchesExtra(product, "destination", request.getDestination()))
            .filter(product -> matchesExtra(product, "depart_city", request.getDepartCity()))
            .filter(product -> matchesExtra(product, "date", request.getDate()))
            .filter(product -> matchesDays(product, request.getMinDays(), request.getMaxDays()))
            .filter(product -> matchesPrice(product, request.getMinPrice(), request.getMaxPrice()))
            .filter(product -> matchesAnyTag(product, request.getTags()))
            .toList();
    }

    @Override
    public Product searchById(Integer productId) {
        return productMapper.selectById(productId);
    }

    @Override
    public VacationOptionsResponse options() {
        List<Product> vacations = selectActiveVacations();
        List<String> tags = vacations.stream()
            .flatMap(product -> product.getCategoryTags() == null ? java.util.stream.Stream.empty() : product.getCategoryTags().stream())
            .filter(tag -> tag != null && !tag.isBlank())
            .distinct()
            .sorted(Comparator.naturalOrder())
            .toList();
        return new VacationOptionsResponse(
            distinctExtraValues(vacations, "destination"),
            distinctExtraValues(vacations, "depart_city"),
            distinctExtraValues(vacations, "date"),
            tags
        );
    }

    private List<Product> selectActiveVacations() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, "VACATION")
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

    private boolean matchesDays(Product product, Integer minDays, Integer maxDays) {
        if (minDays == null && maxDays == null) {
            return true;
        }
        Object value = product.getExtra() == null ? null : product.getExtra().get("days");
        if (value == null) {
            return false;
        }
        int days = Integer.parseInt(String.valueOf(value));
        return (minDays == null || days >= minDays) && (maxDays == null || days <= maxDays);
    }

    private boolean matchesPrice(Product product, Double minPrice, Double maxPrice) {
        double price = product.getPrice() == null ? 0 : product.getPrice();
        return (minPrice == null || price >= minPrice) && (maxPrice == null || price <= maxPrice);
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
