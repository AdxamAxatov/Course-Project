package com.adxam.warehouse.service;

import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;

import java.util.List;

public interface UserService {
    List<User> listAll() throws ServiceException;
    User create(String username, String rawPassword, Role role) throws ServiceException;
    boolean delete(long id) throws ServiceException;
}
