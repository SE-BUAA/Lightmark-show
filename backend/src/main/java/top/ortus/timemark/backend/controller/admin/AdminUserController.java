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
import top.ortus.timemark.backend.service.AdminUserService;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer level
    ) {
        AuthGuards.requireAdmin();
        return ApiResponse.ok(adminUserService.list(page, size, keyword, status, level));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        AuthGuards.requireAdmin();
        adminUserService.updateStatus(id, status, request.getRemoteAddr());
        return ApiResponse.okMessage("操作成功");
    }

    @PatchMapping("/{id}/level")
    public ApiResponse<Void> updateLevel(@PathVariable Long id, @RequestParam Integer level, HttpServletRequest request) {
        AuthGuards.requireAdmin();
        adminUserService.updateLevel(id, level, request.getRemoteAddr());
        return ApiResponse.okMessage("操作成功");
    }
}

