package top.ortus.timemark.backend.service;

import top.ortus.timemark.backend.dto.user.UserCreateRequest;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.user.UserUpdateRequest;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    UserDTO findById(String id);
    UserDTO findByEmail(String email);
    UserDTO findByPhone(String phone);
    UserDTO create(UserCreateRequest request);
    UserDTO update(String id, UserUpdateRequest request);
    boolean delete(String id);
}
