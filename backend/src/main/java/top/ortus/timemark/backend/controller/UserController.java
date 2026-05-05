package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.common.ApiResponse;
import top.ortus.timemark.backend.common.PageResponse;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.user.UserCreateRequest;
import top.ortus.timemark.backend.dto.user.UserUpdateRequest;
import top.ortus.timemark.backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<UserDTO>> listAllUsers() {
        List<UserDTO> users = userService.findAll();
        return ApiResponse.ok(new PageResponse<>(users.size(), users));
    }

    @GetMapping("/id/{id}")
    public ApiResponse<UserDTO> getById(@PathVariable String id) {
        return ApiResponse.ok(userService.findById(id));
    }

    @GetMapping("/phone/{phone}")
    public ApiResponse<UserDTO> getByPhone(@PathVariable String phone) {
        return ApiResponse.ok(userService.findByPhone(phone));
    }

    @GetMapping("/email/{email}")
    public ApiResponse<UserDTO> getByEmail(@PathVariable String email) {
        return ApiResponse.ok(userService.findByEmail(email));
    }

    @PostMapping
    public ApiResponse<UserDTO> create(@RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @PutMapping("/id/{id}")
    public ApiResponse<UserDTO> update(@PathVariable String id, @RequestBody UserUpdateRequest request) {
        return ApiResponse.ok(userService.update(id, request));
    }

    @DeleteMapping("/id/{id}")
    public ApiResponse<Boolean> delete(@PathVariable String id) {
        return ApiResponse.ok(userService.delete(id));
    }
}
