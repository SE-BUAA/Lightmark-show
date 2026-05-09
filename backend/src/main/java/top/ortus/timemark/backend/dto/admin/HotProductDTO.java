package top.ortus.timemark.backend.dto.admin;

public class HotProductDTO {
    private String productId;
    private String name;
    private long soldCount;

    public HotProductDTO() {
    }

    public HotProductDTO(String productId, String name, long soldCount) {
        this.productId = productId;
        this.name = name;
        this.soldCount = soldCount;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(long soldCount) {
        this.soldCount = soldCount;
    }
}
