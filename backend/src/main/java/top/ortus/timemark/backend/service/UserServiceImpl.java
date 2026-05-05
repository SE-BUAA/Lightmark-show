package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.dao.UserRepositoryImpl;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryImpl userRepositoryImpl;

    public UserServiceImpl(UserRepositoryImpl userRepositoryImpl) {
        this.userRepositoryImpl = userRepositoryImpl;
    }


    @Override
    public List<User> findAll() {
        return userRepositoryImpl.findAll();
    }

    @Override
    public User findById(String id) {
        return userRepositoryImpl.findById(id);
    }

    @Override
    public User findByEmail(String email) {
        return userRepositoryImpl.findByEmail(email);
    }

    @Override
    public User findByPhone(String phone) {
        return userRepositoryImpl.findByPhone(phone);
    }
}
