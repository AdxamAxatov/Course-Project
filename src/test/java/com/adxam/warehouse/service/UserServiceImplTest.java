package com.adxam.warehouse.service;

import com.adxam.warehouse.dal.UserDao;
import com.adxam.warehouse.entity.Role;
import com.adxam.warehouse.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    UserDao userDao;

    @InjectMocks
    UserServiceImpl service;

    @Test
    void createHashesPasswordAndPersists() throws Exception {
        when(userDao.findByUsername("bob")).thenReturn(Optional.empty());
        when(userDao.insert(any(User.class))).thenAnswer(inv -> ((User) inv.getArgument(0)).setId(7));

        User result = service.create("bob", "pw", Role.USER);

        assertEquals(7, result.getId());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(captor.capture());
        User saved = captor.getValue();
        assertEquals("bob", saved.getUsername());
        assertEquals(Role.USER, saved.getRole());
        assertNotNull(saved.getSalt());
        assertNotNull(saved.getPasswordHash());
        assertNotEquals("pw", saved.getPasswordHash());
    }

    @Test
    void createRejectsDuplicateUsername() throws Exception {
        when(userDao.findByUsername("bob")).thenReturn(Optional.of(new User().setUsername("bob")));
        assertThrows(ServiceException.class, () -> service.create("bob", "pw", Role.USER));
        verify(userDao, never()).insert(any());
    }

    @Test
    void createValidatesInputs() {
        assertThrows(ServiceException.class, () -> service.create("", "pw", Role.USER));
        assertThrows(ServiceException.class, () -> service.create("u", "", Role.USER));
        assertThrows(ServiceException.class, () -> service.create("u", "pw", null));
    }

    @Test
    void listDelegatesToDao() throws Exception {
        when(userDao.findAll()).thenReturn(List.of(
                new User().setId(1).setUsername("a"),
                new User().setId(2).setUsername("b")));
        List<User> users = service.listAll();
        assertEquals(2, users.size());
    }

    @Test
    void deleteReturnsDaoResult() throws Exception {
        when(userDao.deleteById(3L)).thenReturn(true);
        when(userDao.deleteById(99L)).thenReturn(false);
        assertTrue(service.delete(3L));
        assertFalse(service.delete(99L));
    }
}
