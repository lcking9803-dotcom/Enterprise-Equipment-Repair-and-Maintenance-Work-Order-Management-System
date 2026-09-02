package com.lwq.maintenance.web;

import com.lwq.maintenance.common.ApiResponse;
import com.lwq.maintenance.domain.dto.DashboardSummary;
import com.lwq.maintenance.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER','ACCEPTOR')")
    public ApiResponse<DashboardSummary> summary() {
        return ApiResponse.ok(dashboardService.summary());
    }
}

