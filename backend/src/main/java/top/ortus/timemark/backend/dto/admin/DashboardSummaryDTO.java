package top.ortus.timemark.backend.dto.admin;

import java.math.BigDecimal;

public class DashboardSummaryDTO {
    private long totalUsers;
    private long totalOrders;
    private BigDecimal totalRevenue;

    public DashboardSummaryDTO() {
    }

    public DashboardSummaryDTO(long totalUsers, long totalOrders, BigDecimal totalRevenue) {
        this.totalUsers = totalUsers;
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(BigDecimal totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
}