package com.lwq.maintenance.web;

import com.lwq.maintenance.common.*;
import com.lwq.maintenance.domain.dto.*;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {
    private final WorkOrderService workOrderService;

    @GetMapping
    public ApiResponse<PageResponse<WorkOrder>> page(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String status,
                                                      @RequestParam(required = false) String priority) {
        return ApiResponse.ok(workOrderService.page(page, Math.min(size, 100), keyword, status, priority));
    }

    @GetMapping("/{id}")
    public ApiResponse<WorkOrderDetail> detail(@PathVariable Long id) {
        return ApiResponse.ok(workOrderService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REPORTER','ADMIN')")
    public ApiResponse<WorkOrder> create(@Valid @RequestBody CreateWorkOrderRequest request) {
        return ApiResponse.ok(workOrderService.create(request));
    }

    @PostMapping("/{id}/actions/accept")
    public ApiResponse<WorkOrder> accept(@PathVariable Long id, @Valid @RequestBody AcceptActionRequest request) {
        return ApiResponse.ok(workOrderService.accept(id, request));
    }

    @PostMapping("/{id}/actions/dispatch")
    public ApiResponse<WorkOrder> dispatch(@PathVariable Long id, @Valid @RequestBody DispatchActionRequest request) {
        return ApiResponse.ok(workOrderService.dispatch(id, request));
    }

    @PostMapping("/{id}/actions/repair")
    public ApiResponse<WorkOrder> repair(@PathVariable Long id, @Valid @RequestBody RepairActionRequest request) {
        return ApiResponse.ok(workOrderService.repair(id, request));
    }

    @PostMapping("/{id}/actions/inspection")
    public ApiResponse<WorkOrder> inspect(@PathVariable Long id, @Valid @RequestBody InspectionActionRequest request) {
        return ApiResponse.ok(workOrderService.inspect(id, request));
    }
}

