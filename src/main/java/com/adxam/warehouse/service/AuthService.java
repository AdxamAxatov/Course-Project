package com.adxam.warehouse.service;

import com.adxam.warehouse.entity.User;

import java.util.Optional;

public interface AuthService {
    Optional<User> authenticate(String username, String rawPassword) throws ServiceException;
}
