package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.UserDao;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import com.adxam.warehouse.util.PasswordHasher;

import java.util.List;
import java.util.logging.Logger;

public class UserServiceImpl implements UserService {
    private static final Logger LOG = Logger.getLogger(UserServiceImpl.class.getName());
    private final UserDao userDao;

    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public List<User> listAll() throws ServiceException {
        try {
            return userDao.findAll();
        } catch (DaoException e) {
            throw new ServiceException("Failed to list users: " + e.getMessage());
        }
    }

    @Override
    public User create(String username, String rawPassword, Role role) throws ServiceException {
        if (username == null || username.isBlank()) throw new ServiceException("Username is required");
        if (rawPassword == null || rawPassword.isBlank()) throw new ServiceException("Password is required");
        if (role == null) throw new ServiceException("Role is required");
        try {
            if (userDao.findByUsername(username).isPresent()) {
                throw new ServiceException("Username already exists: " + username);
            }
            String salt = PasswordHasher.newSalt();
            String hash = PasswordHasher.hash(rawPassword, salt);
            User user = new User()
                    .setUsername(username)
                    .setPasswordHash(hash)
                    .setSalt(salt)
                    .setRole(role);
            User saved = userDao.insert(user);
            LOG.info(() -> "User created: " + saved);
            return saved;
        } catch (DaoException e) {
            throw new ServiceException("Failed to create user: " + e.getMessage());
        }
    }

    @Override
    public boolean delete(long id) throws ServiceException {
        try {
            boolean removed = userDao.deleteById(id);
            if (removed) LOG.info(() -> "User deleted: id=" + id);
            return removed;
        } catch (DaoException e) {
            throw new ServiceException("Failed to delete user: " + e.getMessage());
        }
    }
}
