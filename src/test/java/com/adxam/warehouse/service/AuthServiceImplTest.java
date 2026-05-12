package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.DaoException;
import com.adxam.warehouse.dal.UserDao;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import com.adxam.warehouse.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    AuthServiceImpl auth;

    User stored;

    @BeforeEach
    void setUp() {
        String salt = PasswordHasher.newSalt();
        stored = new User()
                .setId(1)
                .setUsername("alice")
                .setSalt(salt)
                .setPasswordHash(PasswordHasher.hash("secret", salt))
                .setRole(Role.USER);
    }

    @Test
    void authenticateReturnsUserWhenPasswordMatches() throws Exception {
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(stored));

        Optional<User> result = auth.authenticate("alice", "secret");

        assertTrue(result.isPresent());
        assertEquals("alice", result.get().getUsername());
    }

    @Test
    void authenticateReturnsEmptyOnWrongPassword() throws Exception {
        when(userDao.findByUsername("alice")).thenReturn(Optional.of(stored));

        Optional<User> result = auth.authenticate("alice", "wrong");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticateReturnsEmptyOnUnknownUser() throws Exception {
        when(userDao.findByUsername("nobody")).thenReturn(Optional.empty());

        Optional<User> result = auth.authenticate("nobody", "x");

        assertTrue(result.isEmpty());
    }

    @Test
    void authenticateThrowsOnNullCredentials() {
        assertThrows(ServiceException.class, () -> auth.authenticate(null, "x"));
        assertThrows(ServiceException.class, () -> auth.authenticate("u", null));
    }

    @Test
    void authenticateWrapsDaoFailure() throws Exception {
        when(userDao.findByUsername("alice")).thenThrow(new DaoException(new RuntimeException("db down")));
        assertThrows(ServiceException.class, () -> auth.authenticate("alice", "secret"));
    }
}
