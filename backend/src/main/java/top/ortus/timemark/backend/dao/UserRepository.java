package top.ortus.timemark.backend.dao;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User findById(String id);

    User findByPhone(String phone);

    User findByEmail(String email);
}
