package com.lwq.maintenance.service;

import com.lwq.maintenance.domain.dto.DashboardSummary;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import com.lwq.maintenance.mapper.DashboardMapper;
import com.lwq.maintenance.mapper.WorkOrderMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DashboardServiceTest {
    @Test
    void metricsUseOnlyClosedOrdersForSlaAndMttr() {
        WorkOrderMapper orderMapper = mock(WorkOrderMapper.class);
        DashboardMapper dashboardMapper = mock(DashboardMapper.class);
        LocalDateTime start = LocalDateTime.of(2026, 8, 1, 8, 0);
        WorkOrder closed = new WorkOrder(); closed.setStatus(WorkOrderStatus.CLOSED); closed.setCreatedAt(start);
        closed.setAcceptedAt(start.plusHours(1)); closed.setClosedAt(start.plusHours(4)); closed.setSlaDeadline(start.plusHours(24));
        WorkOrder open = new WorkOrder(); open.setStatus(WorkOrderStatus.IN_REPAIR); open.setCreatedAt(start);
        open.setAcceptedAt(start.plusHours(3)); open.setSlaDeadline(start.plusHours(48));
        when(orderMapper.selectList(any())).thenReturn(List.of(closed, open));
        when(dashboardMapper.statusDistribution()).thenReturn(List.of());
        when(dashboardMapper.faultRanking()).thenReturn(List.of());
        when(dashboardMapper.technicianWorkload()).thenReturn(List.of());
        when(dashboardMapper.monthlyTrend()).thenReturn(List.of());

        DashboardSummary result = new DashboardService(orderMapper, dashboardMapper).summary();

        assertEquals(2, result.total());
        assertEquals(1, result.open());
        assertEquals("100.00", result.slaComplianceRate().toPlainString());
        assertEquals("2.00", result.averageFirstResponseHours().toPlainString());
        assertEquals("4.00", result.mttrHours().toPlainString());
    }
}

