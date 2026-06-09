package top.ortus.lightmark.backend.dto.admin;

import java.math.BigDecimal;

public class DashboardTrendDTO {
    private String date;
    private int orderCount;
    private BigDecimal revenue;

    public DashboardTrendDTO() {
    }

    public DashboardTrendDTO(String date, int orderCount, BigDecimal revenue) {
        this.date = date;
        this.orderCount = orderCount;
        this.revenue = revenue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getOrderCount() {
        return orderCount;
    }

    public void setOrderCount(int orderCount) {
        this.orderCount = orderCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
