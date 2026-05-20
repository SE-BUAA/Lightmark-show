package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import top.ortus.timemark.backend.dao.Product;
import top.ortus.timemark.backend.dao.ProductMapper;

import java.util.List;

@Service
public class VacationServiceImpl implements VacationService{
    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> search() {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getProductType, "VACATION");
        return productMapper.selectList(queryWrapper);
    }

    @Override
    public Product searchById(Integer productId) {
        return productMapper.selectById(productId);
    }
}
