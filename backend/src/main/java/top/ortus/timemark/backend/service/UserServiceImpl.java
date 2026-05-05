package top.ortus.timemark.backend.service;

import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.converter.UserConverter;
import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.dao.UserRepositoryImpl;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.user.UserCreateRequest;
import top.ortus.timemark.backend.dto.user.UserUpdateRequest;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryImpl userRepositoryImpl;

    public UserServiceImpl(UserRepositoryImpl userRepositoryImpl) {
        this.userRepositoryImpl = userRepositoryImpl;
    }


    @Override
    public List<UserDTO> findAll() {
        List<User> users = userRepositoryImpl.findAll();
        return UserConverter.toDtoList(users);
    }

    @Override
    public UserDTO findById(String id) {
        User user = userRepositoryImpl.findById(id);
        return UserConverter.toDto(user);
    }

    @Override
    public UserDTO findByEmail(String email) {
        User user = userRepositoryImpl.findByEmail(email);
        return UserConverter.toDto(user);
    }

    @Override
    public UserDTO findByPhone(String phone) {
        User user = userRepositoryImpl.findByPhone(phone);
        return UserConverter.toDto(user);
    }

    @Override
    public UserDTO create(UserCreateRequest request) {
        validateCreateRequest(request);
        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setNickname(valueOrDefault(request.getNickname(), ""));
        user.setAvatar(valueOrDefault(request.getAvatar(), ""));
        user.setPoints(valueOrDefault(request.getPoints(), 0));
        user.setLevel(valueOrDefault(request.getLevel(), (short) 0));
        user.setStatus(valueOrDefault(request.getStatus(), 0));
        user.setRegister_source(valueOrDefault(request.getRegister_source(), defaultRegisterSource(request)));
        user.setLast_login_time(request.getLast_login_time());
        user.setLast_login_ip(request.getLast_login_ip());
        user.setCreate_time(now);
        user.setUpdate_time(now);
        user.setDeleted(false);
        userRepositoryImpl.insert(user);
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            return UserConverter.toDto(userRepositoryImpl.findByEmail(request.getEmail()));
        }
        return UserConverter.toDto(userRepositoryImpl.findByPhone(request.getPhone()));
    }

    @Override
    public UserDTO update(String id, UserUpdateRequest request) {
        User user = userRepositoryImpl.findById(id);
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(request.getPassword());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getPoints() != null) {
            user.setPoints(request.getPoints());
        }
        if (request.getLevel() != null) {
            user.setLevel(request.getLevel());
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRegister_source() != null) {
            user.setRegister_source(request.getRegister_source());
        }
        if (request.getLast_login_time() != null) {
            user.setLast_login_time(request.getLast_login_time());
        }
        if (request.getLast_login_ip() != null) {
            user.setLast_login_ip(request.getLast_login_ip());
        }
        user.setUpdate_time(LocalDateTime.now());
        userRepositoryImpl.update(user);
        return UserConverter.toDto(userRepositoryImpl.findById(id));
    }

    @Override
    public boolean delete(String id) {
        return userRepositoryImpl.softDeleteById(id) > 0;
    }

    private void validateCreateRequest(UserCreateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("request is required");
        }
        if ((request.getPhone() == null || request.getPhone().isEmpty())
                && (request.getEmail() == null || request.getEmail().isEmpty())) {
            throw new IllegalArgumentException("phone or email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("password is required");
        }
    }

    private String defaultRegisterSource(UserCreateRequest request) {
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            return "EMAIL";
        }
        return "PHONE";
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

    private int valueOrDefault(Integer value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }

    private short valueOrDefault(Short value, short fallback) {
        if (value == null) {
            return fallback;
        }
        return value;
    }
}
