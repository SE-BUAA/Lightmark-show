package top.ortus.timemark.backend.controller.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.security.AuthGuards;
import top.ortus.timemark.backend.service.AdminOrderService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) Integer status
    ) {
        AuthGuards.requireAdmin();
        return ApiResponse.ok(adminOrderService.list(page, size, keyword, orderType, status));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String cancelReason,
            HttpServletRequest request
    ) {
        AuthGuards.requireAdmin();
        adminOrderService.updateStatus(id, status, cancelReason, request.getRemoteAddr());
        return ApiResponse.okMessage("操作成功");
    }
}

