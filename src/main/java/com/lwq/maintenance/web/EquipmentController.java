package com.lwq.maintenance.web;

import com.lwq.maintenance.common.*;
import com.lwq.maintenance.domain.dto.EquipmentRequest;
import com.lwq.maintenance.domain.entity.Equipment;
import com.lwq.maintenance.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/equipment")
@RequiredArgsConstructor
public class EquipmentController {
    private final EquipmentService equipmentService;

    @GetMapping
    public ApiResponse<PageResponse<Equipment>> page(@RequestParam(defaultValue = "1") long page,
                                                      @RequestParam(defaultValue = "10") long size,
                                                      @RequestParam(required = false) String keyword,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String status) {
        return ApiResponse.ok(equipmentService.page(page, Math.min(size, 100), keyword, category, status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Equipment> get(@PathVariable Long id) {
        return ApiResponse.ok(equipmentService.get(id));
    }

    @GetMapping("/metadata/categories")
    public ApiResponse<List<String>> categories() {
        return ApiResponse.ok(equipmentService.categories());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ApiResponse<Equipment> create(@Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.ok(equipmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DISPATCHER')")
    public ApiResponse<Equipment> update(@PathVariable Long id, @Valid @RequestBody EquipmentRequest request) {
        return ApiResponse.ok(equipmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ApiResponse.ok();
    }
}

