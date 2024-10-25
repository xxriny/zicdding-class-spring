package com.example.zicdding.domain.user.repository;

import com.example.zicdding.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Member;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    static final private String TABLE = "TB_USER";

    private static final RowMapper<User> rowMapper = (ResultSet resultSet, int rowNum) -> User.builder()
            .id(resultSet.getLong("id"))
            .nickname(resultSet.getString("nickname"))
            .email(resultSet.getString("email"))
            .password(resultSet.getString("password"))
            .phoneNumber(resultSet.getString("phoneNumber"))
            .roleType(resultSet.getString("roleType"))
            .suspensionYn(resultSet.getString("suspensionYn"))
            .delYn("N")
            .createdDate(resultSet.getObject("createdDate", LocalDateTime.class))
            .build();

    public Optional<User> findById(Long id) {
        var sql = String.format("SELECT * FROM %s WHERE id = :id",TABLE);
        var params = new MapSqlParameterSource().addValue("id", id);
        List<User> users = namedParameterJdbcTemplate.query(sql, params,rowMapper);

        User nullableUser = DataAccessUtils.singleResult(users);
        return Optional.ofNullable(nullableUser);
    }
    public Optional<User> findByLoginInfoUserId(String email){
        var sql = String.format("SELECT * FROM %s WHERE email = :email",TABLE);
        var params = new MapSqlParameterSource().addValue("email", email);
        List<User> users = namedParameterJdbcTemplate.query(sql, params,rowMapper);

        User nullableUser = DataAccessUtils.singleResult(users);
        return Optional.ofNullable(nullableUser);
    }

    public User save(User user){
        if(user.getId() == null){
            return insert(user);
        }
        return update(user);
    }

    private User update(User user) {
        var sql = String.format("UPDATE %s set nickname = :nickname,password = :password,phone_num = :phoneNumber where user_id = :id ",TABLE);
        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        namedParameterJdbcTemplate.update(sql, params);
        return user;
    }

    public User insert(User user){
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        var id = jdbcInsert.executeAndReturnKey(params).longValue();

        return User.builder()
                .id(id)
                .email(user.getEmail())
                .nickname(user.getNickname())
                .password(user.getPassword())
                .phoneNumber(user.getPhoneNumber())
                .roleType(user.getRoleType())
                .suspensionYn(user.getSuspensionYn())
                .createdDate(user.getCreatedDate())
                .delYn(user.getDelYn())
                .build();
    }
}
