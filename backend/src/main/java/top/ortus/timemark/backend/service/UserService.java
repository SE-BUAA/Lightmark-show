package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dao.User;

import java.util.List;

public interface UserService {
    List<User> findAll();
    User findById(String id);
    User findByEmail(String email);
    User findByPhone(String phone);
}
