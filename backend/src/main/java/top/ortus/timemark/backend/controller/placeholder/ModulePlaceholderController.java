package top.ortus.timemark.backend.controller.placeholder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;

import java.util.Map;

@RestController
public class ModulePlaceholderController {

    @GetMapping("/api/m2/ping")
    public ApiResponse<Map<String, Object>> module2() {
        return notImplemented("M2 用户中心与 AI 智能助手");
    }

    @GetMapping("/api/m3/ping")
    public ApiResponse<Map<String, Object>> module3() {
        return notImplemented("M3 机票预订模块");
    }

    @GetMapping("/api/m4/ping")
    public ApiResponse<Map<String, Object>> module4() {
        return notImplemented("M4 酒店预订模块");
    }

    @GetMapping("/api/m5/ping")
    public ApiResponse<Map<String, Object>> module5() {
        return notImplemented("M5 火车票与旅游度假模块");
    }

    @GetMapping("/api/m6/ping")
    public ApiResponse<Map<String, Object>> module6() {
        return notImplemented("M6 智能行程与社区模块");
    }

    private ApiResponse<Map<String, Object>> notImplemented(String moduleName) {
        return ApiResponse.fail(501, moduleName + " 接口预留中");
    }
}

