package com.lwq.maintenance.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lwq.maintenance.auth.CurrentUser;
import com.lwq.maintenance.auth.UserPrincipal;
import com.lwq.maintenance.common.BusinessException;
import com.lwq.maintenance.common.PageResponse;
import com.lwq.maintenance.domain.dto.*;
import com.lwq.maintenance.domain.entity.*;
import com.lwq.maintenance.domain.enums.EquipmentStatus;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import com.lwq.maintenance.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class WorkOrderService {
    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderLogMapper logMapper;
    private final AttachmentMapper attachmentMapper;
    private final EquipmentMapper equipmentMapper;
    private final SysUserMapper userMapper;

    public PageResponse<WorkOrder> page(long page, long size, String keyword, String status, String priority) {
        UserPrincipal user = CurrentUser.get();
        LambdaQueryWrapper<WorkOrder> query = Wrappers.<WorkOrder>lambdaQuery()
                .and(StringUtils.hasText(keyword), q -> q.like(WorkOrder::getOrderNo, keyword)
                        .or().like(WorkOrder::getTitle, keyword).or().like(WorkOrder::getFaultDescription, keyword))
                .eq(StringUtils.hasText(status), WorkOrder::getStatus, status)
                .eq(StringUtils.hasText(priority), WorkOrder::getPriority, priority)
                .orderByDesc(WorkOrder::getCreatedAt);
        applyDataScope(query, user);
        return PageResponse.of(workOrderMapper.selectPage(Page.of(page, size), query));
    }

    public WorkOrderDetail detail(Long id) {
        WorkOrder order = accessible(id);
        List<WorkOrderLog> logs = logMapper.selectList(Wrappers.<WorkOrderLog>lambdaQuery()
                .eq(WorkOrderLog::getWorkOrderId, id).orderByAsc(WorkOrderLog::getCreatedAt));
        List<Attachment> attachments = attachmentMapper.selectList(Wrappers.<Attachment>lambdaQuery()
                .eq(Attachment::getWorkOrderId, id).orderByDesc(Attachment::getCreatedAt));
        return new WorkOrderDetail(order, logs, attachments);
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public WorkOrder create(CreateWorkOrderRequest request) {
        Equipment equipment = equipmentMapper.selectById(request.equipmentId());
        if (equipment == null) throw BusinessException.notFound("设备不存在");
        UserPrincipal user = CurrentUser.get();
        LocalDateTime now = LocalDateTime.now();
        WorkOrder order = new WorkOrder();
        order.setOrderNo(generateOrderNo());
        order.setEquipmentId(request.equipmentId());
        order.setTitle(request.title());
        order.setFaultType(request.faultType());
        order.setFaultDescription(request.faultDescription());
        order.setPriority(request.priority());
        order.setStatus(WorkOrderStatus.PENDING_ACCEPTANCE);
        order.setReporterId(user.id());
        order.setSlaDeadline(now.plusHours(request.priority().getSlaHours()));
        order.setVersion(0);
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        workOrderMapper.insert(order);
        equipment.setStatus(EquipmentStatus.FAULT);
        equipment.setUpdatedAt(now);
        equipmentMapper.updateById(equipment);
        insertLog(order.getId(), null, order.getStatus(), "CREATE", user, "创建报修工单");
        return order;
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public WorkOrder accept(Long id, AcceptActionRequest request) {
        requireRole("DISPATCHER", "ADMIN");
        WorkOrder order = raw(id);
        ensureStatus(order, WorkOrderStatus.PENDING_ACCEPTANCE);
        UserPrincipal user = CurrentUser.get();
        order.setDispatcherId(user.id());
        order.setAcceptedAt(LocalDateTime.now());
        return transition(order, WorkOrderStatus.PENDING_DISPATCH, "ACCEPT", request.remark(), user);
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public WorkOrder dispatch(Long id, DispatchActionRequest request) {
        requireRole("DISPATCHER", "ADMIN");
        WorkOrder order = raw(id);
        ensureStatus(order, WorkOrderStatus.PENDING_DISPATCH);
        SysUser assignee = userMapper.selectById(request.assigneeId());
        if (assignee == null || !"MAINTAINER".equals(assignee.getRoleCode()) || !Boolean.TRUE.equals(assignee.getEnabled())) {
            throw new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "请选择有效的维修人员");
        }
        UserPrincipal user = CurrentUser.get();
        order.setDispatcherId(user.id());
        order.setAssigneeId(assignee.getId());
        order.setDispatchedAt(LocalDateTime.now());
        return transition(order, WorkOrderStatus.IN_REPAIR, "DISPATCH", request.remark(), user);
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public WorkOrder repair(Long id, RepairActionRequest request) {
        requireRole("MAINTAINER", "ADMIN");
        WorkOrder order = raw(id);
        ensureStatus(order, WorkOrderStatus.IN_REPAIR);
        UserPrincipal user = CurrentUser.get();
        if (!"ADMIN".equals(user.roleCode()) && !user.id().equals(order.getAssigneeId())) {
            throw BusinessException.forbidden("只能处理分配给自己的工单");
        }
        order.setRepairDescription(request.repairDescription());
        order.setRepairCost(request.repairCost());
        order.setRepairedAt(LocalDateTime.now());
        order.setRejectionReason(null);
        return transition(order, WorkOrderStatus.PENDING_INSPECTION, "SUBMIT_REPAIR", request.remark(), user);
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public WorkOrder inspect(Long id, InspectionActionRequest request) {
        requireRole("ACCEPTOR", "ADMIN");
        WorkOrder order = raw(id);
        ensureStatus(order, WorkOrderStatus.PENDING_INSPECTION);
        UserPrincipal user = CurrentUser.get();
        order.setInspectorId(user.id());
        if (request.passed()) {
            order.setClosedAt(LocalDateTime.now());
            WorkOrder result = transition(order, WorkOrderStatus.CLOSED, "INSPECTION_PASS", request.remark(), user);
            Equipment equipment = equipmentMapper.selectById(order.getEquipmentId());
            if (equipment != null) {
                equipment.setStatus(EquipmentStatus.RUNNING);
                equipment.setLastMaintenanceDate(LocalDateTime.now().toLocalDate());
                if (equipment.getMaintenanceCycleDays() != null) {
                    equipment.setNextMaintenanceDate(equipment.getLastMaintenanceDate().plusDays(equipment.getMaintenanceCycleDays()));
                }
                equipment.setUpdatedAt(LocalDateTime.now());
                equipmentMapper.updateById(equipment);
            }
            return result;
        }
        order.setRejectionReason(request.remark());
        return transition(order, WorkOrderStatus.IN_REPAIR, "INSPECTION_REJECT", request.remark(), user);
    }

    public WorkOrder raw(Long id) {
        WorkOrder order = workOrderMapper.selectById(id);
        if (order == null) throw BusinessException.notFound("工单不存在");
        return order;
    }

    public WorkOrder accessible(Long id) {
        WorkOrder order = raw(id);
        UserPrincipal user = CurrentUser.get();
        if ("REPORTER".equals(user.roleCode()) && !user.id().equals(order.getReporterId()))
            throw BusinessException.forbidden("只能查看自己提交的工单");
        if ("MAINTAINER".equals(user.roleCode()) && !user.id().equals(order.getAssigneeId()))
            throw BusinessException.forbidden("只能查看分配给自己的工单");
        return order;
    }

    private WorkOrder transition(WorkOrder order, WorkOrderStatus target, String action,
                                 String remark, UserPrincipal operator) {
        WorkOrderStatus source = order.getStatus();
        order.setStatus(target);
        order.setUpdatedAt(LocalDateTime.now());
        if (workOrderMapper.updateById(order) == 0) {
            throw BusinessException.conflict("工单已被其他用户处理，请刷新后重试");
        }
        insertLog(order.getId(), source, target, action, operator, remark);
        return order;
    }

    private void insertLog(Long orderId, WorkOrderStatus from, WorkOrderStatus to, String action,
                           UserPrincipal operator, String remark) {
        WorkOrderLog log = new WorkOrderLog();
        log.setWorkOrderId(orderId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setAction(action);
        log.setOperatorId(operator.id());
        log.setOperatorName(operator.displayName());
        log.setRemark(remark);
        log.setCreatedAt(LocalDateTime.now());
        logMapper.insert(log);
    }

    private void ensureStatus(WorkOrder order, WorkOrderStatus expected) {
        if (order.getStatus() != expected) {
            throw BusinessException.conflict("非法状态流转：当前状态为 " + order.getStatus() + "，要求状态为 " + expected);
        }
    }

    private void requireRole(String... allowed) {
        String current = CurrentUser.get().roleCode();
        for (String role : allowed) if (role.equals(current)) return;
        throw BusinessException.forbidden("当前角色无权执行该操作");
    }

    private void applyDataScope(LambdaQueryWrapper<WorkOrder> query, UserPrincipal user) {
        if ("REPORTER".equals(user.roleCode())) query.eq(WorkOrder::getReporterId, user.id());
        if ("MAINTAINER".equals(user.roleCode())) query.eq(WorkOrder::getAssigneeId, user.id());
    }

    private String generateOrderNo() {
        return "WO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }
}

