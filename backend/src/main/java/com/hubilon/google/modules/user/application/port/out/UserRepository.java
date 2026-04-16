package com.hubilon.google.modules.user.application.port.out;

import com.hubilon.google.modules.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    boolean existsByEmail(String email);
}
