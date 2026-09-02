package com.lwq.maintenance.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummary(
        long total,
        long open,
        long closed,
        BigDecimal slaComplianceRate,
        BigDecimal averageFirstResponseHours,
        BigDecimal mttrHours,
        List<Map<String, Object>> statusDistribution,
        List<Map<String, Object>> faultRanking,
        List<Map<String, Object>> technicianWorkload,
        List<Map<String, Object>> monthlyTrend) implements java.io.Serializable {
}
