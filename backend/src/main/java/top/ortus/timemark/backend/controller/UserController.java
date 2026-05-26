package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.module.OrderDTO;
import top.ortus.timemark.backend.dto.module.PointsLogDTO;
import top.ortus.timemark.backend.dto.module.TravelerDTO;
import top.ortus.timemark.backend.dto.user.UserAvatarUpdateRequest;
import top.ortus.timemark.backend.dto.user.UserCurrentDTO;
import top.ortus.timemark.backend.dto.user.UserLevelUpgradeInfoDTO;
import top.ortus.timemark.backend.dto.user.UserPasswordUpdateRequest;
import top.ortus.timemark.backend.dto.user.UserUpdateRequest;
import top.ortus.timemark.backend.service.GenericCrudService;
import top.ortus.timemark.backend.service.UserService;
import top.ortus.timemark.backend.security.UserIdentity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器，提供用户相关的 API 接口
 */
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final GenericCrudService genericCrudService;
    private final JwtTokenService jwtTokenService;

    /**
     * 构造函数
     * @param userService 用户服务
     */
    public UserController(UserService userService, GenericCrudService genericCrudService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.genericCrudService = genericCrudService;
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/current")
    public ApiResponse<UserCurrentDTO> current(@RequestHeader("Authorization") String authorization) {
        UserDTO user = resolveCurrentUser(authorization);
        UserIdentity identity = resolveIdentity(authorization);
        return ApiResponse.ok(new UserCurrentDTO(user, identity, Collections.emptyList()));
    }

    @PutMapping("/current")
    public ApiResponse<UserDTO> updateCurrent(@RequestHeader("Authorization") String authorization,
                                              @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.update(resolveUserId(authorization), request));
    }

    @PostMapping("/avatar")
    public ApiResponse<Map<String, String>> uploadAvatar(@RequestHeader("Authorization") String authorization,
                                                         @RequestBody UserAvatarUpdateRequest request) {
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setAvatar(request == null ? null : request.getAvatarUrl());
        userService.update(resolveUserId(authorization), updateRequest);
        return ApiResponse.ok(Map.of("avatarUrl", request == null ? "" : request.getAvatarUrl()));
    }

    @PutMapping("/password")
    public ApiResponse<Boolean> updatePassword(@RequestHeader("Authorization") String authorization,
                                               @RequestBody UserPasswordUpdateRequest request) {
        if (request == null) {
            return ApiResponse.error(400, "invalid request");
        }
        return ApiResponse.ok(userService.updatePassword(resolveUserId(authorization), request.getOldPassword(), request.getNewPassword()));
    }

    @GetMapping("/travelers")
    public ApiResponse<List<TravelerDTO>> listTravelers(@RequestHeader("Authorization") String authorization,
                                                        @RequestParam Map<String, String> params) {
        Map<String, String> filters = new java.util.HashMap<>(params);
        filters.put("user_id", resolveUserId(authorization));
        return ApiResponse.ok(genericCrudService.listTyped("traveler", filters, TravelerDTO.class));
    }

    @PostMapping("/travelers")
    public ApiResponse<TravelerDTO> createTraveler(@RequestHeader("Authorization") String authorization,
                                                   @RequestBody TravelerDTO payload) {
        payload.setUser_id(resolveUserId(authorization));
        return ApiResponse.ok(genericCrudService.createTyped("traveler", payload, TravelerDTO.class));
    }

    @PutMapping("/travelers/{id}")
    public ApiResponse<TravelerDTO> updateTraveler(@PathVariable String id,
                                                   @RequestHeader("Authorization") String authorization,
                                                   @RequestBody TravelerDTO payload) {
        payload.setId(id);
        payload.setUser_id(resolveUserId(authorization));
        return ApiResponse.ok(genericCrudService.updateTyped("traveler", payload, TravelerDTO.class));
    }

    @DeleteMapping("/travelers/{id}")
    public ApiResponse<Boolean> deleteTraveler(@PathVariable String id,
                                               @RequestHeader("Authorization") String authorization) {
        TravelerDTO payload = new TravelerDTO();
        payload.setId(id);
        payload.setUser_id(resolveUserId(authorization));
        return ApiResponse.ok(genericCrudService.deleteTyped("traveler", payload));
    }

    @GetMapping("/points/logs")
    public ApiResponse<PageResponse<PointsLogDTO>> pointsLogs(@RequestHeader("Authorization") String authorization,
                                                              @RequestParam Map<String, String> params) {
        Map<String, String> filters = new java.util.HashMap<>(params);
        filters.put("user_id", resolveUserId(authorization));
        List<PointsLogDTO> items = genericCrudService.listTyped("points_log", filters, PointsLogDTO.class);
        return ApiResponse.ok(toPage(items, params));
    }

    @GetMapping("/level/upgrade-info")
    public ApiResponse<UserLevelUpgradeInfoDTO> levelUpgradeInfo() {
        return ApiResponse.ok(new UserLevelUpgradeInfoDTO((short) 1, 0, List.of("VIP 折扣", "积分加成")));
    }

    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderDTO>> orders(@RequestHeader("Authorization") String authorization,
                                                      @RequestParam Map<String, String> params) {
        Map<String, String> filters = new java.util.HashMap<>(params);
        filters.put("user_id", resolveUserId(authorization));
        List<OrderDTO> items = genericCrudService.listTyped("orders", filters, OrderDTO.class);
        return ApiResponse.ok(toPage(items, params));
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderDTO> orderDetail(@PathVariable String orderNo,
                                             @RequestHeader("Authorization") String authorization) {
        Map<String, String> filters = Map.of("order_no", orderNo, "user_id", resolveUserId(authorization));
        List<OrderDTO> items = genericCrudService.listTyped("orders", filters, OrderDTO.class);
        return ApiResponse.ok(items.isEmpty() ? null : items.get(0));
    }

    private String resolveUserId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "0";
        }
        Long userId = jwtTokenService.resolveUserId(authorization.substring("Bearer ".length()));
        return userId == null ? "0" : String.valueOf(userId);
    }

    private UserDTO resolveCurrentUser(String authorization) {
        String userId = resolveUserId(authorization);
        return userService.findById(userId);
    }

    private UserIdentity resolveIdentity(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return UserIdentity.USER;
        }
        return jwtTokenService.resolveIdentity(authorization.substring("Bearer ".length()));
    }

    private <T> PageResponse<T> toPage(List<T> items, Map<String, String> params) {
        int page = parseInt(params.get("page"), 1);
        int size = parseInt(params.get("size"), items == null ? 0 : items.size());
        return new PageResponse<>(items == null ? 0 : items.size(), page, size, items);
    }

    private int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
