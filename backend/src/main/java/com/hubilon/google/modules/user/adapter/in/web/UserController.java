package com.hubilon.google.modules.user.adapter.in.web;

import com.hubilon.google.common.response.ApiResponse;
import com.hubilon.google.config.multiLanguage.MessageProvider;
import com.hubilon.google.modules.user.application.port.in.GetUserUseCase;
import com.hubilon.google.modules.user.application.port.in.LoginUserUseCase;
import com.hubilon.google.modules.user.application.port.in.RegisterUserUseCase;
import com.hubilon.google.modules.user.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUserUseCase loginUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final MessageProvider messageProvider;

    @PostMapping("/register")
    @Operation(summary = "회원가입")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        User user = registerUserUseCase.register(
                new RegisterUserUseCase.RegisterCommand(
                        request.email(),
                        request.name(),
                        request.password()
                )
        );
        String message = messageProvider.getMessage("user.register.success");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(message, UserResponse.from(user)));
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        LoginUserUseCase.LoginResult result = loginUserUseCase.login(
                new LoginUserUseCase.LoginCommand(request.email(), request.password())
        );
        String message = messageProvider.getMessage("user.login.success");
        return ResponseEntity.ok(ApiResponse.success(message, LoginResponse.from(result)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "사용자 조회 (ID)", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        User user = getUserUseCase.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(UserResponse.from(user)));
    }

    @GetMapping
    @Operation(summary = "전체 사용자 조회", security = @SecurityRequirement(name = "bearerAuth"))
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = getUserUseCase.getAllUsers().stream()
                .map(UserResponse::from)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(users));
    }
}
