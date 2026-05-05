package top.ortus.timemark.backend.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import top.ortus.timemark.backend.JwtTokenService;
import top.ortus.timemark.backend.converter.UserConverter;
import top.ortus.timemark.backend.dao.User;
import top.ortus.timemark.backend.dao.UserRepositoryImpl;
import top.ortus.timemark.backend.dto.UserDTO;
import top.ortus.timemark.backend.dto.auth.AuthLoginRequest;
import top.ortus.timemark.backend.dto.auth.AuthRegisterRequest;
import top.ortus.timemark.backend.dto.auth.AuthTokenDTO;
import top.ortus.timemark.backend.exception.ApiException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthService {

    private final UserRepositoryImpl userRepositoryImpl;
    private final JwtTokenService jwtTokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepositoryImpl userRepositoryImpl, JwtTokenService jwtTokenService) {
        this.userRepositoryImpl = userRepositoryImpl;
        this.jwtTokenService = jwtTokenService;
    }

    public UserDTO register(AuthRegisterRequest request) {
        if (request == null || request.getAccount() == null || request.getAccount().isEmpty()) {
            throw new ApiException(400, "account is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ApiException(400, "password is required");
        }
        try {
            User existing = userRepositoryImpl.findByAccount(request.getAccount());
            if (existing != null) {
                throw new ApiException(409, "account already exists");
            }
        } catch (ApiException ex) {
            throw ex;
        } catch (Exception ex) {
            // ignore if not found
        }
        User user = new User();
        if (request.getAccount().contains("@")) {
            user.setEmail(request.getAccount());
        } else {
            user.setPhone(request.getAccount());
        }
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() == null ? "" : request.getNickname());
        user.setAvatar("");
        user.setPoints(0);
        user.setLevel((short) 0);
        user.setStatus(0);
        user.setRegister_source(request.getAccount().contains("@") ? "EMAIL" : "PHONE");
        user.setCreate_time(LocalDateTime.now());
        user.setUpdate_time(LocalDateTime.now());
        user.setDeleted(false);
        userRepositoryImpl.insert(user);
        User created = userRepositoryImpl.findByAccount(request.getAccount());
        return UserConverter.toDto(created);
    }

    public AuthTokenDTO login(AuthLoginRequest request) {
        if (request == null || request.getAccount() == null || request.getAccount().isEmpty()) {
            throw new ApiException(400, "account is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ApiException(400, "password is required");
        }
        User user;
        try {
            user = userRepositoryImpl.findByAccount(request.getAccount());
        } catch (Exception ex) {
            throw new ApiException(401, "invalid credentials");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ApiException(401, "invalid credentials");
        }
        String token = jwtTokenService.createToken(Long.valueOf(user.getId()), user.getNickname(), List.of("USER"));
        return new AuthTokenDTO(token, user.getId(), user.getNickname());
    }
}
