package top.ortus.timemark.backend.dao;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        String sql = "select * from user where deleted = 0";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public User findById(String id) {
        String sql = "select * from user where id = ? and deleted = 0";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), id);
    }

    @Override
    public User findByPhone(String phone) {
        String sql = "select * from user where phone = ? and deleted = 0";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), phone);
    }

    @Override
    public User findByEmail(String email) {
        String sql = "select * from user where email = ? and deleted = 0";
        return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(User.class), email);
    }
}
