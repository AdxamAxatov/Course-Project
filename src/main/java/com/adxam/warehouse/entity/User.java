package com.adxam.warehouse.entity;

import java.util.Objects;

public class User {
    private long id;
    private String username;
    private String passwordHash;
    private String salt;
    private Role role;

    public User setId(long id) { this.id = id; return this; }
    public User setUsername(String username) { this.username = username; return this; }
    public User setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
    public User setSalt(String salt) { this.salt = salt; return this; }
    public User setRole(Role role) { this.role = role; return this; }

    public long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getSalt() { return salt; }
    public Role getRole() { return role; }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', role=%s}", id, username, role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User other)) return false;
        return id == other.id && Objects.equals(username, other.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}
