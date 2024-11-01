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

import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class UserRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    static final private String TABLE = "TB_USER";

    private static final RowMapper<User> rowMapper = (ResultSet resultSet, int rowNum) -> User.builder()
            .id(resultSet.getLong("user_id"))
            .email(resultSet.getString("email"))
            .password(resultSet.getString("password"))
            .nickname(resultSet.getString("nickname"))
            .phoneNumber(resultSet.getString("phone_num"))
            .roleType(resultSet.getString("role_type"))
            .suspensionYn(resultSet.getString("suspension_yn"))
            .delYn(resultSet.getString("del_yn"))
            .createdDate(resultSet.getObject("created_date", LocalDateTime.class))
            .build();

    public Optional<User> findById(Long id) {
        var sql = String.format("SELECT * FROM %s WHERE user_id = :id",TABLE);
        var params = new MapSqlParameterSource().addValue("id", id);
        List<User> users = namedParameterJdbcTemplate.query(sql, params,rowMapper);

        User nullableUser = DataAccessUtils.singleResult(users);
        return Optional.ofNullable(nullableUser);
    }

    public Optional<User> findByEmail(String email) {
        var sql = String.format("SELECT * FROM %s WHERE email = :email",TABLE);
        var params = new MapSqlParameterSource().addValue("email", email);

        System.out.println("Executing SQL: " + sql);
        System.out.println("With parameters: " + params);
        List<User> users = namedParameterJdbcTemplate.query(sql, params,rowMapper);
        User nullableUser = DataAccessUtils.singleResult(users);

        return Optional.ofNullable(nullableUser);
    }

    public User save(User user){
        if(user.getId() == null){
            System.out.println("Phone number from Repository: " + user.getPhoneNumber());
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

        System.out.println("Inserting User: " + user);
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(namedParameterJdbcTemplate.getJdbcTemplate())
                .withTableName(TABLE)
                .usingGeneratedKeyColumns("user_id");

        SqlParameterSource params = new BeanPropertySqlParameterSource(user);
        var id = jdbcInsert.executeAndReturnKey(params).longValue();
        System.out.println("Inserted id: " + params);
        return User.builder()
                .id(id)
                .email(user.getEmail())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .roleType(user.getRoleType())
                .suspensionYn(user.getSuspensionYn())
                .createdDate(user.getCreatedDate())
                .delYn(user.getDelYn())
                .build();
    }
}
