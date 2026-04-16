package com.hubilon.google.modules.user.application.service;

import com.hubilon.google.common.exception.code.ErrorCode;
import com.hubilon.google.common.exception.custom.ServiceException;
import com.hubilon.google.config.security.JwtProvider;
import com.hubilon.google.modules.user.application.port.in.GetUserUseCase;
import com.hubilon.google.modules.user.application.port.in.LoginUserUseCase;
import com.hubilon.google.modules.user.application.port.in.RegisterUserUseCase;
import com.hubilon.google.modules.user.application.port.out.UserRepository;
import com.hubilon.google.modules.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements RegisterUserUseCase, LoginUserUseCase, GetUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Override
    @Transactional
    public User register(RegisterCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new ServiceException(ErrorCode.USER_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(command.email())
                .name(command.name())
                .password(passwordEncoder.encode(command.password()))
                .role(User.Role.USER)
                .status(User.Status.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    @Override
    public LoginResult login(LoginCommand command) {
        User user = userRepository.findByEmail(command.email())
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));

        if (!user.isActive()) {
            throw new ServiceException(ErrorCode.USER_INACTIVE);
        }

        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            throw new ServiceException(ErrorCode.USER_INVALID_PASSWORD);
        }

        String accessToken = jwtProvider.generateAccessToken(user.getEmail(), user.getRole().name());

        return new LoginResult(
                accessToken,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().name()
        );
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ServiceException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
