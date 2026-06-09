package top.ortus.lightmark.backend.dto.admin.request;

public class AdminOrderStatusRequest {
    private Integer status;
    private String remark;

    public AdminOrderStatusRequest() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
