package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.service.GenericCrudService;

import java.util.List;
import java.util.Map;

public class CrudController {

    private final GenericCrudService genericCrudService;

    public CrudController(GenericCrudService genericCrudService) {
        this.genericCrudService = genericCrudService;
    }

    @GetMapping("/{table}")
    public ApiResponse<PageResponse<Map<String, Object>>> list(@PathVariable String table,
                                                               @RequestParam Map<String, String> params) {
        List<Map<String, Object>> items = genericCrudService.list(table, params);
        return ApiResponse.ok(new PageResponse<>(items.size(), items));
    }

    @GetMapping("/{table}/{id}")
    public ApiResponse<Map<String, Object>> getById(@PathVariable String table, @PathVariable String id) {
        return ApiResponse.ok(genericCrudService.getById(table, id));
    }

    @PostMapping("/{table}")
    public ApiResponse<Map<String, Object>> create(@PathVariable String table,
                                                   @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(genericCrudService.create(table, payload));
    }

    @PutMapping("/{table}")
    public ApiResponse<Map<String, Object>> update(@PathVariable String table,
                                                   @RequestBody Map<String, Object> payload) {
        return ApiResponse.ok(genericCrudService.update(table, payload));
    }

    @DeleteMapping("/{table}")
    public ApiResponse<Boolean> delete(@PathVariable String table,
                                       @RequestBody(required = false) Map<String, Object> payload,
                                       @RequestParam Map<String, String> params) {
        if (payload == null || payload.isEmpty()) {
            return ApiResponse.ok(genericCrudService.delete(table, Map.copyOf(params)));
        }
        return ApiResponse.ok(genericCrudService.delete(table, payload));
    }
}