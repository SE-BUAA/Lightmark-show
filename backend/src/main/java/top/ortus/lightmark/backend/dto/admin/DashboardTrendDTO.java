package top.ortus.lightmark.backend.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public class DashboardTrendDTO {
    private List<String> dates;
    private List<Integer> orderCounts;
    private List<BigDecimal> revenues;

    public DashboardTrendDTO() {
    }

    public DashboardTrendDTO(List<String> dates, List<Integer> orderCounts, List<BigDecimal> revenues) {
        this.dates = dates;
        this.orderCounts = orderCounts;
        this.revenues = revenues;
    }

    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Integer> getOrderCounts() {
        return orderCounts;
    }

    public void setOrderCounts(List<Integer> orderCounts) {
        this.orderCounts = orderCounts;
    }

    public List<BigDecimal> getRevenues() {
        return revenues;
    }

    public void setRevenues(List<BigDecimal> revenues) {
        this.revenues = revenues;
    }
}
