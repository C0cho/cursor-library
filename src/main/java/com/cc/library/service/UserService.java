package com.cc.library.service;

import com.cc.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    User getUserByUsername(String username);
    Page<User> getAllUsers(Pageable pageable);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
} 