package top.ortus.timemark.backend.converter;

import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.dto.UserDTO;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserConverter {

    private UserConverter() {
    }

    public static UserDTO toDto(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getPhone(),
                user.getEmail(),
                user.getNickname(),
                user.getAvatar(),
                user.getPoints(),
                user.getLevel(),
                user.getStatus(),
                user.getRegister_source(),
                user.getLast_login_time(),
                user.getLast_login_ip(),
                user.getCreate_time(),
                user.getUpdate_time(),
                user.isDeleted()
        );
    }

    public static List<UserDTO> toDtoList(List<User> users) {
        if (users == null || users.isEmpty()) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(UserConverter::toDto)
                .collect(Collectors.toList());
    }
}

