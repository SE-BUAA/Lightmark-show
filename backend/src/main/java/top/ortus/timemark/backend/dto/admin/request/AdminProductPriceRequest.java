package top.ortus.timemark.backend.dto.admin.request;

import java.math.BigDecimal;

public class AdminProductPriceRequest {
    private BigDecimal price;

    public AdminProductPriceRequest() {
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
