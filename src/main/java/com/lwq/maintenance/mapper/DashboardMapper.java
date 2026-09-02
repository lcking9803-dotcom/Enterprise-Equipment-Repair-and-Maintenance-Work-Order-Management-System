package com.lwq.maintenance.mapper;

import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface DashboardMapper {
    @Select("SELECT status AS name, COUNT(*) AS `value` FROM work_order WHERE deleted=0 GROUP BY status ORDER BY COUNT(*) DESC")
    List<Map<String, Object>> statusDistribution();

    @Select("SELECT fault_type AS name, COUNT(*) AS `value` FROM work_order WHERE deleted=0 GROUP BY fault_type ORDER BY COUNT(*) DESC LIMIT 8")
    List<Map<String, Object>> faultRanking();

    @Select("SELECT COALESCE(u.display_name, '未派单') AS name, COUNT(*) AS `value` " +
            "FROM work_order w LEFT JOIN sys_user u ON w.assignee_id=u.id " +
            "WHERE w.deleted=0 GROUP BY u.display_name ORDER BY COUNT(*) DESC LIMIT 10")
    List<Map<String, Object>> technicianWorkload();

    @Select("SELECT CONCAT(t.y, '-', LPAD(t.m, 2, '0')) AS `month`, COUNT(*) AS created_count, " +
            "SUM(CASE WHEN t.status='CLOSED' THEN 1 ELSE 0 END) AS closed_count " +
            "FROM (SELECT EXTRACT(YEAR FROM created_at) AS y, EXTRACT(MONTH FROM created_at) AS m, status " +
            "FROM work_order WHERE deleted=0) t GROUP BY t.y, t.m ORDER BY t.y DESC, t.m DESC LIMIT 12")
    List<Map<String, Object>> monthlyTrend();

}
