package com.lwq.maintenance.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.lwq.maintenance.domain.dto.DashboardSummary;
import com.lwq.maintenance.domain.entity.WorkOrder;
import com.lwq.maintenance.domain.enums.WorkOrderStatus;
import com.lwq.maintenance.mapper.DashboardMapper;
import com.lwq.maintenance.mapper.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DashboardService {
    private final WorkOrderMapper workOrderMapper;
    private final DashboardMapper dashboardMapper;

    @Cacheable(cacheNames = "dashboard", key = "'summary'")
    public DashboardSummary summary() {
        List<WorkOrder> orders = workOrderMapper.selectList(Wrappers.<WorkOrder>query()
                .select("status", "created_at", "accepted_at", "closed_at", "sla_deadline"));
        long total = orders.size();
        long closed = orders.stream().filter(o -> o.getStatus() == WorkOrderStatus.CLOSED).count();
        long open = total - closed;
        long withinSla = orders.stream().filter(o -> o.getStatus() == WorkOrderStatus.CLOSED && o.getClosedAt() != null
                && o.getSlaDeadline() != null && !o.getClosedAt().isAfter(o.getSlaDeadline())).count();
        double avgResponse = orders.stream().filter(o -> o.getAcceptedAt() != null)
                .mapToLong(o -> Duration.between(o.getCreatedAt(), o.getAcceptedAt()).toMinutes()).average().orElse(0) / 60.0;
        double mttr = orders.stream().filter(o -> o.getClosedAt() != null)
                .mapToLong(o -> Duration.between(o.getCreatedAt(), o.getClosedAt()).toMinutes()).average().orElse(0) / 60.0;
        return new DashboardSummary(total, open, closed, percent(withinSla, closed), decimal(avgResponse), decimal(mttr),
                dashboardMapper.statusDistribution(), dashboardMapper.faultRanking(),
                dashboardMapper.technicianWorkload(), dashboardMapper.monthlyTrend());
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator == 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(numerator * 100.0 / denominator).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
