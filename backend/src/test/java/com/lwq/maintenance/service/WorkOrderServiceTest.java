package com.lwq.maintenance.service;

import com.lwq.maintenance.auth.UserPrincipal;
import com.lwq.maintenance.common.BusinessException;
import com.lwq.maintenance.domain.dto.*;
import com.lwq.maintenance.domain.entity.SysUser;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.domain.entity.WorkOrderLog;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import com.lwq.maintenance.mapper.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {
    @Mock WorkOrderMapper workOrderMapper;
    @Mock WorkOrderLogMapper logMapper;
    @Mock AttachmentMapper attachmentMapper;
    @Mock EquipmentMapper equipmentMapper;
    @Mock SysUserMapper userMapper;
    WorkOrderService service;

    @BeforeEach
    void setUp() {
        service = new WorkOrderService(workOrderMapper, logMapper, attachmentMapper, equipmentMapper, userMapper);
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void duplicateAcceptIsRejectedAsIllegalTransition() {
        authenticate(3L, "DISPATCHER");
        WorkOrder order = order(WorkOrderStatus.PENDING_DISPATCH);
        when(workOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.accept(1L, new AcceptActionRequest("重复受理")));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(workOrderMapper, never()).updateById(any(WorkOrder.class));
    }

    @Test
    void dispatchRequiresEnabledMaintainer() {
        authenticate(3L, "DISPATCHER");
        when(workOrderMapper.selectById(1L)).thenReturn(order(WorkOrderStatus.PENDING_DISPATCH));
        SysUser invalid = new SysUser(); invalid.setId(9L); invalid.setRoleCode("REPORTER"); invalid.setEnabled(true);
        when(userMapper.selectById(9L)).thenReturn(invalid);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.dispatch(1L, new DispatchActionRequest(9L, null)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
    }

    @Test
    void maintainerCannotRepairSomebodyElsesOrder() {
        authenticate(4L, "MAINTAINER");
        WorkOrder order = order(WorkOrderStatus.IN_REPAIR); order.setAssigneeId(6L);
        when(workOrderMapper.selectById(1L)).thenReturn(order);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.repair(1L, new RepairActionRequest("更换零件", null, null)));

        assertEquals(HttpStatus.FORBIDDEN, error.getStatus());
    }

    @Test
    void rejectedInspectionReturnsOrderToRepairAndWritesAuditLog() {
        authenticate(5L, "ACCEPTOR");
        WorkOrder order = order(WorkOrderStatus.PENDING_INSPECTION); order.setAssigneeId(4L);
        when(workOrderMapper.selectById(1L)).thenReturn(order);
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(1);

        WorkOrder result = service.inspect(1L, new InspectionActionRequest(false, "振动仍超标"));

        assertEquals(WorkOrderStatus.IN_REPAIR, result.getStatus());
        assertEquals("振动仍超标", result.getRejectionReason());
        verify(logMapper).insert(ArgumentMatchers.<WorkOrderLog>argThat(log -> "INSPECTION_REJECT".equals(log.getAction())
                && log.getFromStatus() == WorkOrderStatus.PENDING_INSPECTION
                && log.getToStatus() == WorkOrderStatus.IN_REPAIR));
    }

    @Test
    void optimisticLockConflictDoesNotWriteAuditLog() {
        authenticate(3L, "DISPATCHER");
        when(workOrderMapper.selectById(1L)).thenReturn(order(WorkOrderStatus.PENDING_ACCEPTANCE));
        when(workOrderMapper.updateById(any(WorkOrder.class))).thenReturn(0);

        BusinessException error = assertThrows(BusinessException.class,
                () -> service.accept(1L, new AcceptActionRequest(null)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        verify(logMapper, never()).insert(any(WorkOrderLog.class));
    }

    private WorkOrder order(WorkOrderStatus status) {
        WorkOrder order = new WorkOrder(); order.setId(1L); order.setStatus(status); order.setVersion(0);
        order.setReporterId(2L); order.setEquipmentId(1L); return order;
    }

    private void authenticate(Long id, String role) {
        UserPrincipal principal = new UserPrincipal(id, role.toLowerCase(), "", role + "用户", role, true);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}
