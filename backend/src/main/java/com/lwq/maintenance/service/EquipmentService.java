package com.lwq.maintenance.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lwq.maintenance.common.BusinessException;
import com.lwq.maintenance.common.PageResponse;
import com.lwq.maintenance.domain.dto.EquipmentRequest;
import com.lwq.maintenance.domain.entity.Equipment;
import com.lwq.maintenance.mapper.EquipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipmentService {
    private final EquipmentMapper equipmentMapper;

    public PageResponse<Equipment> page(long page, long size, String keyword, String category, String status) {
        var query = Wrappers.<Equipment>lambdaQuery()
                .and(StringUtils.hasText(keyword), q -> q.like(Equipment::getEquipmentCode, keyword)
                        .or().like(Equipment::getName, keyword).or().like(Equipment::getLocation, keyword))
                .eq(StringUtils.hasText(category), Equipment::getCategory, category)
                .eq(StringUtils.hasText(status), Equipment::getStatus, status)
                .orderByDesc(Equipment::getUpdatedAt);
        return PageResponse.of(equipmentMapper.selectPage(Page.of(page, size), query));
    }

    public Equipment get(Long id) {
        Equipment equipment = equipmentMapper.selectById(id);
        if (equipment == null) throw BusinessException.notFound("设备不存在");
        return equipment;
    }

    @Cacheable(cacheNames = "equipment-categories", key = "'all'")
    public List<String> categories() {
        return equipmentMapper.selectObjs(Wrappers.<Equipment>query()
                        .select("DISTINCT category").orderByAsc("category")).stream()
                .map(String::valueOf).toList();
    }

    @Transactional
    @CacheEvict(cacheNames = {"equipment-categories", "dashboard"}, allEntries = true)
    public Equipment create(EquipmentRequest request) {
        Equipment equipment = apply(new Equipment(), request);
        equipment.setVersion(0);
        equipment.setDeleted(0);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipmentMapper.insert(equipment);
        return equipment;
    }

    @Transactional
    @CacheEvict(cacheNames = {"equipment-categories", "dashboard"}, allEntries = true)
    public Equipment update(Long id, EquipmentRequest request) {
        Equipment equipment = get(id);
        apply(equipment, request);
        equipment.setUpdatedAt(LocalDateTime.now());
        if (equipmentMapper.updateById(equipment) == 0) {
            throw BusinessException.conflict("设备已被其他用户修改，请刷新后重试");
        }
        return equipment;
    }

    @Transactional
    @CacheEvict(cacheNames = {"equipment-categories", "dashboard"}, allEntries = true)
    public void delete(Long id) {
        if (equipmentMapper.deleteById(id) == 0) throw BusinessException.notFound("设备不存在");
    }

    private Equipment apply(Equipment equipment, EquipmentRequest request) {
        equipment.setEquipmentCode(request.equipmentCode());
        equipment.setName(request.name());
        equipment.setCategory(request.category());
        equipment.setLocation(request.location());
        equipment.setDepartment(request.department());
        equipment.setResponsiblePerson(request.responsiblePerson());
        equipment.setStatus(request.status());
        equipment.setMaintenanceCycleDays(request.maintenanceCycleDays());
        equipment.setLastMaintenanceDate(request.lastMaintenanceDate());
        if (request.lastMaintenanceDate() != null && request.maintenanceCycleDays() != null) {
            equipment.setNextMaintenanceDate(request.lastMaintenanceDate().plusDays(request.maintenanceCycleDays()));
        }
        equipment.setDescription(request.description());
        return equipment;
    }
}

