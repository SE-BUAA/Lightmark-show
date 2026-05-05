package top.ortus.timemark.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.service.UserService;
import top.ortus.timemark.backend.service.UserServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserServiceImpl userServiceImpl;

    public UserController(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @GetMapping
    public List<User> listAllUsers() {
        return userServiceImpl.findAll();
    }

    @GetMapping("/id/{id}")
    public User getById(@PathVariable String id) {
        return userServiceImpl.findById(id);
    }

    @GetMapping("/phone/{phone}")
    public User getOne(@PathVariable String phone) {
        return userServiceImpl.findByPhone(phone);
    }
}
