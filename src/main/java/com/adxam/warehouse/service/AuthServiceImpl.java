package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.UserDao;
import com.adxam.warehouse.entity.User;
import com.adxam.warehouse.util.PasswordHasher;

import java.util.Optional;
import java.util.logging.Logger;

public class AuthServiceImpl implements AuthService {
    private static final Logger LOG = Logger.getLogger(AuthServiceImpl.class.getName());
    private final UserDao userDao;

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public Optional<User> authenticate(String username, String rawPassword) throws ServiceException {
        if (username == null || rawPassword == null) {
            throw new ServiceException("Username and password are required");
        }
        try {
            Optional<User> found = userDao.findByUsername(username);
            if (found.isEmpty()) {
                LOG.fine(() -> "Authentication failed: unknown user " + username);
                return Optional.empty();
            }
            User user = found.get();
            if (PasswordHasher.matches(rawPassword, user.getSalt(), user.getPasswordHash())) {
                return Optional.of(user);
            }
            LOG.fine(() -> "Authentication failed: bad password for " + username);
            return Optional.empty();
        } catch (DaoException e) {
            throw new ServiceException("Authentication error: " + e.getMessage());
        }
    }
}
