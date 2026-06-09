package top.ortus.lightmark.backend.dto.module;

import java.time.LocalDateTime;

public class ProductViewLogDTO {
    private String id;
    private String user_id;
    private String product_id;
    private LocalDateTime view_time;
    private String ip;

    public ProductViewLogDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public void setProduct_id(String product_id) {
        this.product_id = product_id;
    }

    public LocalDateTime getView_time() {
        return view_time;
    }

    public void setView_time(LocalDateTime view_time) {
        this.view_time = view_time;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}