package com.lwq.maintenance.auth;

import com.lwq.maintenance.common.BusinessException;
import com.lwq.maintenance.domain.dto.LoginRequest;
import com.lwq.maintenance.domain.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
            return new LoginResponse(jwtService.generate(user), user.id(), user.username(),
                    user.displayName(), user.roleCode());
        } catch (AuthenticationException e) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
        }
    }
}

