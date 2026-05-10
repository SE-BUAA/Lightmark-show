package top.ortus.timemark.backend.dto.admin.request;

import java.math.BigDecimal;

public class AdminProductCreateRequest {
    private String productType;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private Integer soldCount;
    private Integer status;
    private String categoryTags;
    private String extra;

    public AdminProductCreateRequest() {
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = soldCount;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCategoryTags() {
        return categoryTags;
    }

    public void setCategoryTags(String categoryTags) {
        this.categoryTags = categoryTags;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
