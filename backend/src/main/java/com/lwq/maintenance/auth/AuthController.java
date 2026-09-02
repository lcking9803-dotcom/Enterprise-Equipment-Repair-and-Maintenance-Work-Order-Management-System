package com.lwq.maintenance.auth;

import com.lwq.maintenance.common.ApiResponse;
import com.lwq.maintenance.domain.dto.LoginRequest;
import com.lwq.maintenance.domain.dto.LoginResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        UserPrincipal user = CurrentUser.get();
        return ApiResponse.ok(Map.of("id", user.id(), "username", user.username(),
                "displayName", user.displayName(), "roleCode", user.roleCode()));
    }
}

