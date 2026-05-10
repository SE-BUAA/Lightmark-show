package top.ortus.timemark.backend.dto.admin.request;

public class AdminCommentApproveRequest {
    private Integer isApproved;

    public AdminCommentApproveRequest() {
    }

    public Integer getIsApproved() {
        return isApproved;
    }

    public void setIsApproved(Integer isApproved) {
        this.isApproved = isApproved;
    }
}
