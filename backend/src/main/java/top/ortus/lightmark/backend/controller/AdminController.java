package top.ortus.lightmark.backend.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.lightmark.backend.JwtTokenService;
import top.ortus.lightmark.backend.common.ApiResponse;
import top.ortus.lightmark.backend.common.PageResponse;
import top.ortus.lightmark.backend.dto.UserDTO;
import top.ortus.lightmark.backend.dto.admin.AdminProductDTO;
import top.ortus.lightmark.backend.dto.admin.AdminOrderDTO;
import top.ortus.lightmark.backend.dto.admin.DashboardSummaryDTO;
import top.ortus.lightmark.backend.dto.admin.DashboardTrendDTO;
import top.ortus.lightmark.backend.dto.admin.HotProductDTO;
import top.ortus.lightmark.backend.dto.admin.request.AdminCommentApproveRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminOrderRefundRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminOrderStatusRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminProductCreateRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminProductPriceRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminProductStatusRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminProductStockRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminQuestionAnswerRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminUserLevelRequest;
import top.ortus.lightmark.backend.dto.admin.request.AdminUserStatusRequest;
import top.ortus.lightmark.backend.dto.module.AdminLogDTO;
import top.ortus.lightmark.backend.dto.module.CommentDTO;
import top.ortus.lightmark.backend.dto.module.QuestionDTO;
import top.ortus.lightmark.backend.dto.module.ProductDTO;
import top.ortus.lightmark.backend.service.AdminService;
import top.ortus.lightmark.backend.service.GenericCrudService;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器，提供后台管理相关的 API 接口
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final GenericCrudService genericCrudService;
    private final JwtTokenService jwtTokenService;

    /**
     * 构造函数
     * @param adminService 管理员服务
     * @param jwtTokenService JWT 令牌服务
     */
    public AdminController(AdminService adminService, GenericCrudService genericCrudService, JwtTokenService jwtTokenService) {
        this.adminService = adminService;
        this.genericCrudService = genericCrudService;
        this.jwtTokenService = jwtTokenService;
    }

    /**
     * 获取仪表板摘要信息
     * @return 仪表板摘要数据
     */
    @GetMapping("/dashboard/summary")
    public ApiResponse<DashboardSummaryDTO> dashboardSummary() {
        return ApiResponse.ok(adminService.dashboardSummary());
    }

    @GetMapping("/dashboard/trends")
    public ApiResponse<DashboardTrendDTO> dashboardTrends() {
        return ApiResponse.ok(adminService.dashboardTrends());
    }

    @GetMapping("/dashboard/hot-products")
    public ApiResponse<List<HotProductDTO>> hotProducts() {
        return ApiResponse.ok(adminService.hotProducts());
    }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserDTO>> listUsers(@RequestParam(required = false) String keyword,
                                                        @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(adminService.listUsers(keyword, status));
    }

    @PutMapping("/users/{id}/status")
    public ApiResponse<Boolean> updateUserStatus(@PathVariable String id,
                                                 @RequestBody AdminUserStatusRequest body,
                                                 HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateUserStatus(id, body.getStatus(), resolveAdminId(servletRequest)));
    }

    @PutMapping("/users/{id}/level")
    public ApiResponse<Boolean> updateUserLevel(@PathVariable String id,
                                                @RequestBody AdminUserLevelRequest body,
                                                HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateUserLevel(id, body.getLevel(), resolveAdminId(servletRequest)));
    }

    @GetMapping("/products")
    public ApiResponse<PageResponse<AdminProductDTO>> listProducts(@RequestParam(required = false) String productType,
                                                                    @RequestParam(required = false) String name,
                                                                    @RequestParam(required = false) Integer status) {
        return ApiResponse.ok(adminService.listProducts(productType, name, status));
    }

    @PutMapping("/products/{id}/status")
    public ApiResponse<Boolean> updateProductStatus(@PathVariable String id,
                                                    @RequestBody AdminProductStatusRequest body,
                                                    HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateProductStatus(id, body.getStatus(), resolveAdminId(servletRequest)));
    }

    @PutMapping("/products/{id}/price")
    public ApiResponse<Boolean> updateProductPrice(@PathVariable String id,
                                                   @RequestBody AdminProductPriceRequest body,
                                                   HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateProductPrice(id, body.getPrice(), resolveAdminId(servletRequest)));
    }

    @PutMapping("/products/{id}/stock")
    public ApiResponse<Boolean> updateProductStock(@PathVariable String id,
                                                   @RequestBody AdminProductStockRequest body,
                                                   HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateProductStock(id, body.getStock(), resolveAdminId(servletRequest)));
    }

    @PostMapping("/products")
    public ApiResponse<ProductDTO> createProduct(@RequestBody AdminProductCreateRequest body,
                                                 HttpServletRequest servletRequest) {
        if (body == null) {
            body = new AdminProductCreateRequest();
        }
        ProductDTO product = new ProductDTO();
        product.setProduct_type(body.getProductType());
        product.setName(body.getName());
        product.setPrice(body.getPrice());
        product.setStock(body.getStock() == null ? 0 : body.getStock());
        product.setSold_count(body.getSoldCount() == null ? 0 : body.getSoldCount());
        product.setStatus(body.getStatus() == null ? 1 : body.getStatus());
        product.setCategory_tags(body.getCategoryTags());
        product.setExtra(body.getExtra());
        return ApiResponse.ok(adminService.createProduct(product, resolveAdminId(servletRequest)));
    }

    @DeleteMapping("/products/{id}")
    public ApiResponse<Boolean> deleteProduct(@PathVariable String id,
                                              HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.deleteProduct(id, resolveAdminId(servletRequest)));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<AdminOrderDTO>> listOrders(@RequestParam(required = false) Integer status) {
        return ApiResponse.ok(adminService.listOrders(status));
    }

    @PutMapping("/orders/{orderNo}/status")
    public ApiResponse<Boolean> updateOrderStatus(@PathVariable String orderNo,
                                                  @RequestBody AdminOrderStatusRequest body,
                                                  HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.updateOrderStatus(orderNo, body.getStatus(), body.getRemark(), resolveAdminId(servletRequest)));
    }

    @PostMapping("/orders/{orderNo}/refund")
    public ApiResponse<Boolean> refundOrder(@PathVariable String orderNo,
                                            @RequestBody(required = false) AdminOrderRefundRequest body,
                                            HttpServletRequest servletRequest) {
        return ApiResponse.ok(adminService.refundOrder(orderNo, body == null ? null : body.getRemark(), resolveAdminId(servletRequest)));
    }

    @GetMapping("/logs")
    public ApiResponse<PageResponse<AdminLogDTO>> logs(@RequestParam Map<String, String> params) {
        return ApiResponse.ok(adminService.listAdminLogs(params));
    }

    @PostMapping("/questions/{id}/answer")
    public ApiResponse<Boolean> answerQuestion(@PathVariable String id,
                                               @RequestBody AdminQuestionAnswerRequest body,
                                                HttpServletRequest servletRequest) {
        QuestionDTO payload = new QuestionDTO();
        payload.setId(id);
        payload.setAnswer(body.getAnswer());
        payload.setAnswer_user_id(String.valueOf(resolveAdminId(servletRequest)));
        return ApiResponse.ok(genericCrudService.updateTyped("question", payload, QuestionDTO.class) != null);
    }

    @PutMapping("/comments/{id}/approve")
    public ApiResponse<Boolean> approveComment(@PathVariable String id,
                                                @RequestBody AdminCommentApproveRequest body) {
        CommentDTO payload = new CommentDTO();
        payload.setId(id);
        payload.setIs_approved(body.getIsApproved());
        return ApiResponse.ok(genericCrudService.updateTyped("comment", payload, CommentDTO.class) != null);
    }

    /**
     * 从请求中解析管理员 ID
     * @param request HTTP 请求
     * @return 管理员 ID，解析失败返回默认值 1
     */
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