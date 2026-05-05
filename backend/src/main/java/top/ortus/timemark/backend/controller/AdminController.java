package top.ortus.timemark.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.admin.AdminOrderDTO;
import top.ortus.timemark.backend.dto.admin.AdminProductDTO;
import top.ortus.timemark.backend.dto.admin.DashboardSummaryDTO;
import top.ortus.timemark.backend.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final JwtTokenService jwtTokenService;

    public AdminController(AdminService adminService, JwtTokenService jwtTokenService) {
        this.adminService = adminService;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/dashboard/summary")
    public ApiResponse<DashboardSummaryDTO> dashboardSummary() {
        return ApiResponse.ok(adminService.dashboardSummary());
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<AdminProductDTO>> listProducts(@RequestParam(required = false) String productType) {
        return ApiResponse.ok(adminService.listProducts(productType));
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserDTO>> listUsers(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(adminService.listUsers(keyword));
    }

    @PatchMapping("/users/{id}/status")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable String id,
                                                 @RequestParam int status,
                                                 HttpServletRequest request) {
        return ApiResponse.ok(adminService.updateUserStatus(id, status, resolveAdminId(request)));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<AdminOrderDTO>> listOrders(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(adminService.listOrders(status));
    }

    @PatchMapping("/orders/{id}/status")
    public ApiResponse<Boolean> updateOrderStatus(@PathVariable String id,
                                                  @RequestParam int status,
                                                  @RequestParam(required = false) String cancelReason,
                                                  HttpServletRequest request) {
        return ApiResponse.ok(adminService.updateOrderStatus(id, status, cancelReason, resolveAdminId(request)));
    }

    private Long resolveAdminId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return 1L;
        }
        String token = header.substring("Bearer ".length());
        Long userId = jwtTokenService.resolveUserId(token);
        return userId == null ? 1L : userId;
    }
}

