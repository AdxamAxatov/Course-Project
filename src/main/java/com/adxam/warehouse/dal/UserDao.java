package com.adxam.warehouse.dal;

import com.adxam.warehouse.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByUsername(String username) throws DaoException;
    Optional<User> findById(long id) throws DaoException;
    List<User> findAll() throws DaoException;
    User insert(User user) throws DaoException;
    boolean deleteById(long id) throws DaoException;
}
