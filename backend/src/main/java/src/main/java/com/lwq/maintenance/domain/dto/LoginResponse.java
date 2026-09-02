package com.lwq.maintenance.domain.dto;

public record LoginResponse(String token, Long userId, String username, String displayName, String roleCode) {
}

